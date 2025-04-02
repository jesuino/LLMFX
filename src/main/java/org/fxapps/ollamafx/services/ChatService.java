package org.fxapps.ollamafx.services;

import java.util.List;
import java.util.function.Consumer;

import org.fxapps.ollamafx.Model.Message;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.service.tool.ToolProvider;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ChatService {

    @Inject
    ChatModelFactory modelFactory;

    @Inject
    OllamaService ollamaService;

    public interface ChatBot {

        String chat(String userMessage);

    }

    public interface AsyncChatBot {

        TokenStream chat(String userMessage);

    }

    public String chatSync(String userMessage, String model, ToolProvider toolProvider) {
        var m = modelFactory.getModelSync(ollamaService.getOllamaUrl(), model);
        var bot = AiServices.builder(ChatBot.class)
                .chatLanguageModel(m)
                .toolProvider(toolProvider)
                .build();
        return bot.chat(userMessage);
    }

    public void chatAsync(org.fxapps.ollamafx.Model.ChatRequest chatRequest) {

        var memory = memoryFromMessages(chatRequest.messages());
        var model = modelFactory.getModel(ollamaService.getOllamaUrl(), chatRequest.model());
        var bot = AiServices.builder(AsyncChatBot.class)
                .streamingChatLanguageModel(model)
                .chatMemory(memory)
                .toolProvider(chatRequest.toolProvider())
                .build();

        bot.chat(chatRequest.message())
                .onPartialResponse(chatRequest.onToken())
                .onRetrieved(contents -> System.out.println(contents))
                .onToolExecuted(execution -> System.out.println(execution))
                .onCompleteResponse(chatRequest.onComplete())
                .onError(chatRequest.onError())
                .start();
    }

    private ChatMemory memoryFromMessages(List<Message> messages) {
        return new ChatMemory() {

            @Override
            public Object id() {
                return 1;
            }

            @Override
            public void add(ChatMessage message) {
                // NOP
            }

            @Override
            public List<ChatMessage> messages() {
                return messages.stream().map(m -> {
                    return switch (m.role()) {
                        case USER -> new UserMessage(m.content());
                        case ASSISTANT -> new AiMessage(m.content());
                    };

                }).toList();
            }

            @Override
            public void clear() {
                // NOP
            }

        };
    }

}
