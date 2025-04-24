package org.fxapps.llmfx.tools.graphics;

import org.fxapps.llmfx.Events.NewHTMLContentEvent;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class JFXWebRenderingTool {

    @Inject
    Event<NewHTMLContentEvent> newHTMLContentEvent;

    @Tool("""
            Render and allow users to visualize HTML content. You can use this tool to render HTML content for the user.

            Make sure the HTML contains all the CSS and javascript used by it inside the HTML content, external files will not work.
            """)
    public void renderHTML(@P("The HTML content to be rendered") String html) {
        newHTMLContentEvent.fire(new NewHTMLContentEvent(html));
    }

}
