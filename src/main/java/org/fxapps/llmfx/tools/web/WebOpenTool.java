package org.fxapps.llmfx.tools.web;

import java.io.IOException;

import org.jsoup.Jsoup;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;

@Singleton
public class WebOpenTool {


    @Tool("Fetch the content of a given web page")
    public String getWebPageContent(@P("The URL of the web page to be fetched") String url) throws IOException {
        return Jsoup.connect(url).get().body().text();
    }

    
}
