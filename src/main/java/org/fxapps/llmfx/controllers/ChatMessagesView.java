package org.fxapps.llmfx.controllers;


import jakarta.inject.Singleton;
import javafx.scene.web.WebView;
import org.apache.commons.text.StringEscapeUtils;

import org.fxapps.llmfx.Model.Message;
import org.w3c.dom.html.HTMLElement;

import java.net.URL;

/*
 * This class will be responsible for rendering and displaying the chat messages in the WebView
 */
@Singleton
public class ChatMessagesView {

    private static final String CHAT_PAGE = """
        <html>
            <body>
                <div id="chatContent" class="chat-container">
                </div>
            </body>
        </html>
        """;

    public static final String STYLE = "/style/chat-messages.css";

    private WebView chatOutput;
    private boolean autoScroll;

    public void init(WebView webView) {
        this.chatOutput = webView;
        URL styleUrl = getClass().getResource(STYLE);
        webView.getEngine().setUserStyleSheetLocation(styleUrl.toExternalForm());
        webView.getEngine().loadContent(CHAT_PAGE, "text/html");

        chatOutput.setOnScroll(e -> autoScroll = false);

    }

    public void appendUserMessage(Message userMessage) {
        var message = new StringBuffer("<p>");
        String escapeMessage = StringEscapeUtils.escapeHtml4(userMessage.text());
        message.append(escapeMessage);
        userMessage.content()
                .stream()
                .map(content -> "data:" + content.mimeType() + ";base64, " + content.content())
                .findAny()
                .map(content -> "<br /><img src=\"" + content + "\" />")
                .ifPresent(message::append);
        message.append("</p>");
        runScriptToAppendMessage(message.toString(), "user");
    }

    public void appendSystemMessage(String systemMessage) {
        runScriptToAppendMessage("<p>" + systemMessage + "</p>", "system");
    }

    public void appendAssistantMessage(String assistantMessage) {
        var message = assistantMessage.replaceAll("<think>",
                """
                        <div class="think-box">
                            <h4>Thinking</h4>                            
                        """)
                .replaceAll("</think>",
                        "><h4>end thinking</h4></div>");
                // TODO: find someway to copy to the clipboard
                //.replaceAll("<code", "<code");
        runScriptToAppendMessage(message, "assistant");
    }

    private void runScriptToAppendMessage(String message, String role) {
        // workaround because I don't have an innerHTML method in Java API!
        var script = """
                var messageContent = document.createElement('p');
                var tmp = document.querySelector('#tmp');
                messageContent.setAttribute("class", '%s-message');
                messageContent.innerHTML = tmp.textContent;
                document.querySelector('#chatContent').appendChild(messageContent);
                tmp.remove();
                """.formatted(role);

        var el = chatOutput.getEngine().getDocument().createElement("p");
        el.setTextContent(message);
        el.setAttribute("id", "tmp");
        el.setAttribute("hidden", "true");
        var chatRoot = (HTMLElement) chatOutput.getEngine().getDocument().getElementById("chatContent");
        chatRoot.appendChild(el);
        chatOutput.getEngine().executeScript(script);
        if (autoScroll) {
            chatOutput.getEngine().executeScript("window.scrollTo(0, document.body.scrollHeight);");
        }
    }

    public void setAutoScroll(boolean autoScroll) {
        this.autoScroll = autoScroll;
    }

    public String getChatHistoryHTML() {
        return (String) chatOutput.getEngine().executeScript("document.documentElement.outerHTML");
    }

    public void clearChatHistory() {
        chatOutput.getEngine().executeScript("document.getElementById('chatContent').innerHTML = ''");
    }

}
