package org.fxapps.llmfx.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.fxapps.llmfx.config.LLMConfig;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;

@ApplicationScoped
public class OpenAiService {

    @Inject
    LLMConfig llmConfig;

    public List<String> listModels() throws Exception {
        var httpClient = HttpClient.newHttpClient();
        var endpoint = URI.create(getBaseUrl() + "/models");
        var requestBuilder = HttpRequest.newBuilder()
                .uri(endpoint)
                .header("Accepts", "application/json");

        llmConfig.key().ifPresent(key -> requestBuilder.header("Authorization", "Bearer " + key));

        var request = requestBuilder.build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        var json = Json.createReader(response.body()).read();
        return json.asJsonObject()
                .get("data")
                .asJsonArray()
                .stream()
                .map(j -> j.asJsonObject().getString("id"))
                .toList();

    }

    public String getBaseUrl() {
        var baseUrl = llmConfig.url();
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

    }

}
