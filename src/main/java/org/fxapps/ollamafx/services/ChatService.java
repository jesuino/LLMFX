package org.fxapps.ollamafx.services;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ChatService {

    @Inject
    OllamaService ollamaService;

    @ConfigProperty(name = "ollama.requestTimeout", defaultValue = "120")
    Integer requestTimeout;

    Map<String, StreamingChatLanguageModel> modelCache;

    @PostConstruct
    public void init() {
        modelCache = new HashMap<>();
    }

    public interface AsyncChatBot {

        TokenStream chat(String userMessage);

    }

    public void chatAsync(org.fxapps.ollamafx.Model.ChatRequest chatRequest) {
        var memory = MessageWindowChatMemory.withMaxMessages(100);
        var model = modelCache.computeIfAbsent(chatRequest.model(),
                m -> OllamaStreamingChatModel.builder().baseUrl(ollamaService.getOllamaUrl())
                        .modelName(m)
                        .timeout(Duration.ofSeconds(requestTimeout))
                        .logRequests(true)
                        .logResponses(true)
                        .build());
        var bot = AiServices.builder(AsyncChatBot.class)
                .streamingChatLanguageModel(model)
                .chatMemory(memory)
                .toolProvider(chatRequest.toolProvider())
                .build();

        chatRequest.messages().stream().map(m -> switch (m.role()) {
            case USER -> new UserMessage(m.content());
            case ASSISTANT -> new AiMessage(m.content());

        }).forEach(memory::add);

        bot.chat(chatRequest.message())
                .onPartialResponse(chatRequest.onToken())
                .onRetrieved(contents -> System.out.println(contents))
                .onToolExecuted(execution -> System.out.println(execution))
                .onCompleteResponse(chatRequest.onComplete())
                .onError(chatRequest.onError())
                .start();
    }
}