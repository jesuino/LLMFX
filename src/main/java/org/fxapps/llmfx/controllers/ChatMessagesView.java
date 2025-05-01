package org.fxapps.llmfx.controllers;

import org.apache.commons.text.StringEscapeUtils;
import org.fxapps.llmfx.Model.Message;
import org.w3c.dom.html.HTMLElement;

import jakarta.inject.Singleton;
import javafx.scene.web.WebView;

/*
 * This class will be responsible for rendering and displaying the chat messages in the WebView
 */
@Singleton
public class ChatMessagesView {

    final String CHAT_PAGE = """
                <html>
                    <style>
                        * {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen-Sans, Ubuntu, Cantarell, sans-serif;
                        }
                        .think-box {
                            font-style: italic !important;
                            border-left: 4px solid rgb(124, 132, 143);
                            padding: 0 0 0 10px
                            color: lightgray;
                        }
                        .think-box > h4 {
                            color: red !important;
                        }
                        img {
                            height: auto;
                            width: 100%;
                            max-height: 300px;
                        }

                        table {
                            border-collapse: collapse;
                            margin-bottom: 20px;
                            border-radius: 8px;
                            overflow: hidden;
                        }

                        th, td {
                            padding: 12px 16px;
                            text-align: left;
                            vertical-align: top;
                            background-color: #fafafa;
                            border-bottom: 1px solid #eaeaea;
                        }

                        th {
                            background-color: #1a73e8;
                            color: white;
                            font-weight: 500;
                        }

                        th:last-child {
                            border-bottom: none;
                        }

                        tr:last-child td {
                            border-bottom: none;
                        }

                        .chat-container {
                            padding: 16px;
                            flex-grow: 1;
                            line-height: 1.5;
                        }

                        .chat-container > p {
                            border-radius: 12px;
                            color: #1f1f1f;
                            margin: 8px 0;
                            padding: 12px 16px !important;
                            box-shadow: 0 1px 3px rgba(0,0,0,0.1);
                        }

                        .user-message {
                            background-color: #e3f2fd;
                        }

                        .system-message {
                            background-color: #f5f5f5;
                            border-left: 4px solid #1a73e8;
                            color: #757575 !important;
                            font-style: italic;
                        }

                        .assistant-message {
                            background-color: #eeeeee;
                        }
                    </style>
                    <body>
                        <div id="chatContent" class="chat-container">
                        </div>
                    </body>
                </html>
            """;
    private WebView chatOutput;
    private boolean autoScroll;

    public void init(WebView webView) {
        this.chatOutput = webView;
        webView.getEngine().loadContent(CHAT_PAGE);
        chatOutput.setOnScroll(_ -> autoScroll = false);
    }

    public void appendUserMessage(Message userMessage) {
        var message = new StringBuffer("<p>");
        String escapeMessage = StringEscapeUtils.escapeHtml4(userMessage.text());
        message.append(escapeMessage);
        userMessage.content()
                .stream()
                .map(content -> "data:" + content.mimeType() + ";base64, " + content.content())
                .findAny()
                .ifPresent(content -> message.append("<br /><img src=\"" + content + "\"/>"));
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
        if (autoScroll)
            chatOutput.getEngine().executeScript("window.scrollTo(0, document.body.scrollHeight);");
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
