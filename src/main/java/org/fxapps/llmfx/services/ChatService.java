package org.fxapps.llmfx.services;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.fxapps.llmfx.Model.ChatRequest;
import org.fxapps.llmfx.config.LLMConfig;
import org.jboss.logging.Logger;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.VideoContent;
import dev.langchain4j.http.client.jdk.JdkHttpClient;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.DefaultToolExecutor;
import dev.langchain4j.service.tool.ToolExecutor;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ChatService {

    private static final Logger LOGGER = Logger.getLogger(ChatService.class);

    @Inject
    OpenAiService openAi;

    @Inject
    LLMConfig llmConfig;

    private final Map<String, StreamingChatModel> modelCache;

    private final JdkHttpClientBuilder jdkHttpClientBuilder;

    ChatService() {
        this.modelCache = new HashMap<>();
        // some LLM servers (e.g. lmstudio) require HTTP/1.1
        this.jdkHttpClientBuilder = JdkHttpClient.builder()
                .httpClientBuilder(HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1));
    }

    public interface AsyncChatBot {
        TokenStream chat(String userMessage);
    }

    public void chatAsync(ChatRequest chatRequest) {
        if (chatRequest.content().isEmpty()) {
            this.chat(chatRequest);
        } else {
            this.multimodalChat(chatRequest);
        }

    }

    private void chat(ChatRequest chatRequest) {
        var memory = buildChatMemory(chatRequest);
        var model = getModel(chatRequest);

        var botBuilder = AiServices.builder(AsyncChatBot.class)
                .streamingChatModel(model)
                .chatMemory(memory);

        var tools = getRequestTools(chatRequest);
        botBuilder.tools(tools);
        var tokenStream = botBuilder.build().chat(chatRequest.message());
        applyTokenStreamToRequest(chatRequest, tokenStream).start();

    }

    private void multimodalChat(ChatRequest chatRequest) {
        var memory = buildChatMemory(chatRequest);
        var model = getModel(chatRequest);
        memory.add(messageWithContent(chatRequest));

        var request = dev.langchain4j.model.chat.request.ChatRequest.builder()
                .messages(memory.messages())
                .toolSpecifications(new ArrayList<>(getRequestTools(chatRequest).keySet()))
                .build();
        _multimodalChat(chatRequest, model, request);
    }

    private UserMessage messageWithContent(ChatRequest chatRequest) {
        var contentList = new ArrayList<Content>();
        contentList.add(TextContent.from(chatRequest.message()));
        chatRequest.content()
                .stream()
                .map(c -> switch (c.type()) {
                    case IMAGE -> ImageContent.from(c.content(), "image/png");
                    case AUDIO -> AudioContent.from(c.content());
                    case PDF -> PdfFileContent.from(c.content());
                    case VIDEO -> VideoContent.from(c.content());
                }).forEach(contentList::add);
        return new UserMessage(contentList);
    }

    private void _multimodalChat(ChatRequest chatRequest,
            StreamingChatModel model,
            dev.langchain4j.model.chat.request.ChatRequest request) {
        var requesTools = getRequestTools(chatRequest);
        model.chat(request, new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String token) {
                if (chatRequest.isRunning()) {
                    chatRequest.onToken().accept(token);
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                if (!chatRequest.isRunning()) {
                    return;
                }
                var aiMessage = response.aiMessage();
                if (aiMessage.hasToolExecutionRequests()) {
                    LOGGER.warn("LLM asked for tools execution");
                    var toolExecutionMessages = new ArrayList<ToolExecutionResultMessage>();

                    aiMessage.toolExecutionRequests().forEach(req -> {
                        LOGGER.warn(req);
                        var spec = requesTools.keySet()
                                .stream()
                                .filter(s -> req.name().equals(s.name()))
                                .findFirst();
                        if (spec.isEmpty()) {
                            toolExecutionMessages.add(ToolExecutionResultMessage.from(req,
                                    "No Tool executor found for this tool request"));
                            return;
                        }
                        var executor = requesTools.get(spec.get());
                        try {
                            final var result = executor.execute(req, null);
                            LOGGER.warn(result);
                            toolExecutionMessages.add(ToolExecutionResultMessage.from(req, result));
                        } catch (Exception e) {
                            LOGGER.debug("Error executing tool " + req, e);
                            toolExecutionMessages.add(ToolExecutionResultMessage.from(req, e.getMessage()));
                        }

                    });
                    request.messages().addAll(toolExecutionMessages);
                    // recursive call now with tool calling results
                    _multimodalChat(chatRequest, model, request);
                } else {
                    chatRequest.onComplete().accept(response);
                }
            }

            @Override
            public void onError(Throwable e) {
                if (chatRequest.isRunning()) {
                    chatRequest.onError().accept(e);
                    LOGGER.error("Error during LLM Service call", e);
                } else {
                    LOGGER.trace("Error during LLM Service call", e);
                }
            }

        });
    }

    private StreamingChatModel getModel(ChatRequest chatRequest) {
        return modelCache.computeIfAbsent(chatRequest.model(),
                m -> OpenAiStreamingChatModel.builder()
                        .httpClientBuilder(jdkHttpClientBuilder)
                        .baseUrl(openAi.getBaseUrl())
                        .modelName(m)
                        .apiKey(llmConfig.key().orElse(""))
                        .timeout(Duration.ofSeconds(llmConfig.timeout()))
                        .logRequests(llmConfig.logRequests().orElse(false))
                        .logResponses(llmConfig.logResponses().orElse(false))
                        .build());
    }

    private TokenStream applyTokenStreamToRequest(ChatRequest chatRequest, TokenStream tokenStream) {

        return tokenStream.onPartialResponse(token -> {
            if (chatRequest.isRunning()) {
                chatRequest.onToken().accept(token);
            }

        }).onRetrieved(contents -> {
            if (chatRequest.isRunning()) {
                LOGGER.info("Content retrieved: " + contents);
            }
        }).onToolExecuted(execution -> {
            if (chatRequest.isRunning()) {
                LOGGER.info("Tool Execution: " + execution);
            }
        }).onCompleteResponse(response -> {
            if (chatRequest.isRunning()) {
                chatRequest.onComplete().accept(response);
            }
        }).onError(e -> {
            if (chatRequest.isRunning()) {
                chatRequest.onError().accept(e);
                LOGGER.error("Error during LLM Service call", e);
            } else {
                LOGGER.debug("Error during LLM Service call", e);
            }
        });

    }

    private MessageWindowChatMemory buildChatMemory(ChatRequest chatRequest) {
        var memory = MessageWindowChatMemory.withMaxMessages(100);

        chatRequest.history()
                .stream()
                .filter(m -> !m.text().equals(chatRequest.message()))
                .map(m -> switch (m.role()) {
                    case USER -> new UserMessage(m.text());
                    case ASSISTANT -> new AiMessage(m.text());
                    case SYSTEM -> new SystemMessage(m.text());

                }).forEach(memory::add);
        return memory;
    }

    private HashMap<ToolSpecification, ToolExecutor> getRequestTools(ChatRequest chatRequest) {
        var tools = new HashMap<ToolSpecification, ToolExecutor>();
        // MCP Tools
        chatRequest.mcpClients().forEach(client -> client.listTools()
                .stream()
                .forEach(tool -> tools.put(tool, (req, mem) -> client.executeTool(req))));
        // native tools
        chatRequest.tools().forEach(tool -> {
            ToolSpecifications.toolSpecificationsFrom(tool)
                    .forEach(spec -> tools.put(spec,
                            (req, mem) -> new DefaultToolExecutor(tool, req).execute(req, mem)));
        });
        return tools;
    }
}