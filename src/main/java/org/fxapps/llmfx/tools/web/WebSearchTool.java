package org.fxapps.llmfx.tools.web;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;

@Singleton
public class WebSearchTool {

    private final static String DUCKDUCKGO_SEARCH_URL = "https://duckduckgo.com/html/?q=";

    record WebResult(String link, String title, String description) {
    }

    @Tool("""
            Returns a list of search results for a given query.
            You can use it if users ask you to search the web or
             if you don't know the answer to certain questions.
            """)
    public List<WebResult> doWebSearch(@P("The query to be searched on the internet") String query)
            throws Exception {
        // source:
        // https://medium.com/@sethsubr/fetch-duckduckgo-web-search-results-in-20-lines-of-java-code-3a34ea9da085
        Document doc = null;
        try {
            final var url = DUCKDUCKGO_SEARCH_URL + URLEncoder.encode(query, "UTF-8");
            doc = Jsoup.connect(url).get();
            var results = doc.getElementById("links").getElementsByClass("results_links");
            List<WebResult> searchResults = new ArrayList<>();

            for (Element result : results) {
                var linkTag = result.getElementsByClass("links_main").first().getElementsByTag("a").first();
                var link = linkTag.attr("href");
                var title = linkTag.text();
                var snippet = result.getElementsByClass("result__snippet").first().text();
                searchResults.add(new WebResult(link, title, snippet));
            }
            return searchResults;
        } catch (IOException e) {
            throw e;
        }
    }
}
