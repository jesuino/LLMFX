package org.fxapps.llmfx.services;

import java.net.http.HttpClient;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.fxapps.llmfx.Model.ChatRequest;
import org.fxapps.llmfx.config.LLMConfig;
import org.jboss.logging.Logger;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.VideoContent;
import dev.langchain4j.http.client.jdk.JdkHttpClient;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequestBuilder;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ChatService {

    @Inject
    OpenAiService openAi;

    @Inject
    LLMConfig llmConfig;

    Logger logger = Logger.getLogger(ChatService.class);

    Map<String, StreamingChatLanguageModel> modelCache;

    private JdkHttpClientBuilder jdkHttpClientBuilder;

    @PostConstruct
    public void init() {
        modelCache = new HashMap<>();

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

    void chat(ChatRequest chatRequest) {
        var memory = buildChatMemory(chatRequest);
        var model = getModel(chatRequest);

        var botBuilder = AiServices.builder(AsyncChatBot.class)
                .streamingChatLanguageModel(model)
                .chatMemory(memory);

        // Selected tools will ignore tool provider (MCP)
        if (chatRequest.tools() != null && !chatRequest.tools().isEmpty()) {
            botBuilder.tools(chatRequest.tools());
        } else {
            botBuilder.toolProvider(chatRequest.toolProvider());
        }
        var tokenStream = botBuilder.build().chat(chatRequest.message());
        applyTokenStreamToRequest(chatRequest, tokenStream).start();

    }

    public void multimodalChat(ChatRequest chatRequest) {
        var memory = buildChatMemory(chatRequest);
        var model = getModel(chatRequest);
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

        var request = new ChatRequestBuilder()
                .messages(memory.messages())
                .message(new UserMessage(contentList))
                .build();

        model.chat(request, new StreamingChatResponseHandler() {

            @Override
            public void onPartialResponse(String token) {
                if (chatRequest.isRunning()) {
                    chatRequest.onToken().accept(token);
                }

            }

            @Override
            public void onCompleteResponse(ChatResponse response) {
                if (response.aiMessage().hasToolExecutionRequests()) {
                    logger.warn("Function calls with image not supported at the moment");
                }

            }

            @Override
            public void onError(Throwable e) {
                if (chatRequest.isRunning()) {
                    chatRequest.onError().accept(e);
                    logger.error("Error during LLM Service call", e);
                } else {
                    logger.debug("Error during LLM Service call", e);
                }
            }

        });

    }

    private StreamingChatLanguageModel getModel(ChatRequest chatRequest) {
        return modelCache.computeIfAbsent(chatRequest.model(),
                m -> OpenAiStreamingChatModel.builder()
                        .httpClientBuilder(jdkHttpClientBuilder)
                        .baseUrl(openAi.getBaseUrl())
                        .modelName(m)
                        .apiKey(llmConfig.key().orElse(""))
                        .timeout(Duration.ofSeconds(llmConfig.timeout()))
                        .logRequests(true)
                        .logResponses(true)
                        .build());
    }

    private MessageWindowChatMemory buildChatMemory(ChatRequest chatRequest) {
        var memory = MessageWindowChatMemory.withMaxMessages(100);

        chatRequest.history().stream().map(m -> switch (m.role()) {
            case USER -> new UserMessage(m.text());
            case ASSISTANT -> new AiMessage(m.text());
            case SYSTEM -> new SystemMessage(m.text());

        }).forEach(memory::add);
        return memory;
    }

    private TokenStream applyTokenStreamToRequest(ChatRequest chatRequest, TokenStream tokenStream) {

        return tokenStream.onPartialResponse(token -> {
            if (chatRequest.isRunning()) {
                chatRequest.onToken().accept(token);
            }

        }).onRetrieved(contents -> {
            if (chatRequest.isRunning()) {
                logger.info("Content retrieved: " + contents);
            }
        }).onToolExecuted(execution -> {
            if (chatRequest.isRunning()) {
                logger.info("Tool Execution: " + execution);
            }
        }).onCompleteResponse(response -> {
            if (chatRequest.isRunning()) {
                chatRequest.onComplete().accept(response);
            }
        }).onError(e -> {
            if (chatRequest.isRunning()) {
                chatRequest.onError().accept(e);
                logger.error("Error during LLM Service call", e);
            } else {
                logger.debug("Error during LLM Service call", e);
            }
        });

    }
}