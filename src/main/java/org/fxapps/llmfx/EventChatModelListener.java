package org.fxapps.llmfx;

import org.fxapps.llmfx.Events.ChatModelRequestEvent;
import org.fxapps.llmfx.Events.ChatModelResponseEvent;
import org.fxapps.llmfx.Events.ChatModelErrorEvent;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class EventChatModelListener implements ChatModelListener {

    @Inject
    Event<ChatModelRequestEvent> chatModelRequestEvent;
    @Inject
    Event<ChatModelResponseEvent> chatModelResponseEvent;
    @Inject
    Event<ChatModelErrorEvent> chatModelErrorEvent;

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        chatModelRequestEvent.fire(new ChatModelRequestEvent(requestContext));
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        chatModelResponseEvent.fire(new ChatModelResponseEvent(responseContext));
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        chatModelErrorEvent.fire(new ChatModelErrorEvent(errorContext));
    }

}
