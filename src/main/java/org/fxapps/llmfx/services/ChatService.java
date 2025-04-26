package org.fxapps.llmfx.services;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.fxapps.llmfx.config.LLMConfig;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.http.client.jdk.JdkHttpClient;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
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

    public void chatAsync(org.fxapps.llmfx.Model.ChatRequest chatRequest) {
        var memory = MessageWindowChatMemory.withMaxMessages(100);
        var model = modelCache.computeIfAbsent(chatRequest.model(),
                m -> OpenAiStreamingChatModel.builder()
                        .httpClientBuilder(jdkHttpClientBuilder)
                        .baseUrl(openAi.getBaseUrl())
                        .modelName(m)
                        .apiKey(llmConfig.key().orElse(""))
                        .timeout(Duration.ofSeconds(llmConfig.timeout()))
                        .logRequests(true)
                        .logResponses(true)
                        .build());

        var botBuilder = AiServices.builder(AsyncChatBot.class)
                .streamingChatLanguageModel(model)
                .chatMemory(memory);

        // Selected tools will ignore tool provider (MCP)
        if (chatRequest.tools() != null && !chatRequest.tools().isEmpty()) {
            botBuilder.tools(chatRequest.tools());
        } else {
            botBuilder.toolProvider(chatRequest.toolProvider());
        }

        var bot = botBuilder.build();

        chatRequest.messages().stream().map(m -> switch (m.role()) {
            case USER -> new UserMessage(m.content());
            case ASSISTANT -> new AiMessage(m.content());
            case SYSTEM -> new SystemMessage(m.content());

        }).forEach(memory::add);

        bot.chat(chatRequest.message())
                .onPartialResponse(token -> {
                    if (!chatRequest.stop().get()) {
                        chatRequest.onToken().accept(token);
                        // TODO: force streaming stop here
                    }

                })
                .onRetrieved(contents -> System.out.println(contents))
                .onToolExecuted(execution -> System.out.println(execution))
                .onCompleteResponse(chatRequest.onComplete())
                .onError(chatRequest.onError())
                .start();
    }
}