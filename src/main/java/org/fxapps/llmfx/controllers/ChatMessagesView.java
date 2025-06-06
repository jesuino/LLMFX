package org.fxapps.llmfx.controllers;

import java.net.URL;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.fxapps.llmfx.Model.Message;

import jakarta.inject.Singleton;
import javafx.scene.web.WebView;

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

    private Parser markDownParser;
    private HtmlRenderer markdownRenderer;

    public void init(WebView webView) {
        this.chatOutput = webView;
        this.markdownRenderer = HtmlRenderer.builder().extensions(List.of(TablesExtension.create())).build();
        this.markDownParser = Parser.builder().extensions(List.of(TablesExtension.create())).build();
        URL styleUrl = getClass().getResource(STYLE);
        webView.getEngine().setUserStyleSheetLocation(styleUrl.toExternalForm());
        webView.getEngine().loadContent(CHAT_PAGE, "text/html");

        chatOutput.setOnScroll(e -> {
            // scrolling up
            if (e.getDeltaY() > 0) {
                autoScroll = false;
            }
        });
    }

    public void appendUserMessage(Message userMessage) {
        var message = new StringBuffer("<p>");
        String escapeMessage = StringEscapeUtils.escapeHtml4(userMessage.text());
        message.append(escapeMessage);
        autoScroll = true;
        userMessage.content()
                .stream()
                .map(content -> "data:" + content.mimeType() + ";base64, " + content.content())
                .findAny()
                .map(content -> "<br /><img src=\"" + content + "\" />")
                .ifPresent(message::append);
        message.append("</p>");
        runScriptToAppendMessage(message.toString(), "user", false);
    }

    public void appendSystemMessage(Message systemMessage) {
        runScriptToAppendMessage("<p>" + systemMessage.text() + "</p>", "system", false);
    }

    public void streamAssistantMessage(Message assistantMessage) {
        appendAssistantMessage(assistantMessage.text(), true);
    }

    public void appendAssistantMessage(Message assistantMessage) {
        appendAssistantMessage(assistantMessage.text(), false);
    }

    private void appendAssistantMessage(String assistantMessage, boolean streaming) {
        var assistantHTMLMessage = parseMarkdowToHTML(assistantMessage);
        var message = assistantHTMLMessage
            // qwen 3 generates empty think tags when /nothink is used
            .replaceAll("<think>\\s*</think>", "")
            .replaceAll("<think>",
                """
                        <div class="think-box">
                            <h4>Thinking</h4>
                        """)
                .replaceAll("</think>",
                        "><h4>end thinking</h4></div>");
        // TODO: find someway to copy code to the clipboard
        // .replaceAll("<code", "<code");
        runScriptToAppendMessage(message, "assistant", streaming);
    }

    private void runScriptToAppendMessage(String message, String role, boolean streaming) {
        // workaround because I don't have an innerHTML method in Java API! (and setting
        // the attribute directly does not work)
        var script = """
                var tmp = document.querySelector('#tmp');
                var lastMessage = document.querySelector('#last-message');
                """;
        if (streaming) {
            script += "lastMessage.innerHTML = tmp.textContent;";
        } else {
            script += """
                        if (lastMessage) {
                            lastMessage.id = "";
                        }
                        var newMessage = document.createElement('p');
                        newMessage.id = 'last-message';
                        newMessage.setAttribute("class", '%s-message');
                        newMessage.innerHTML = tmp.textContent;
                        document.querySelector('#chatContent').appendChild(newMessage);
                    """.formatted(role);
        }
        script += "tmp.remove();";

        var tmp = chatOutput.getEngine().getDocument().createElement("p");
        tmp.setTextContent(message);
        tmp.setAttribute("id", "tmp");
        tmp.setAttribute("hidden", "true");
        chatOutput.getEngine().getDocument().getElementById("chatContent").appendChild(tmp);
        chatOutput.getEngine().executeScript(script);
        if (autoScroll) {
            chatOutput.getEngine().executeScript("window.scrollTo(0, document.body.scrollHeight);");
        }
    }

    public String getChatHistoryHTML() {
        return (String) chatOutput.getEngine().executeScript("document.documentElement.outerHTML");
    }

    public void clearChatHistory() {
        chatOutput.getEngine().executeScript("document.getElementById('chatContent').innerHTML = ''");
    }

    private String parseMarkdowToHTML(String markdown) {
        var parsedContent = markDownParser.parse(markdown);
        return markdownRenderer.render(parsedContent);
    }
}
