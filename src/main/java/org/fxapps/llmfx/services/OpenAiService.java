package org.fxapps.llmfx.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.fxapps.llmfx.config.LLMConfig;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;

@ApplicationScoped
public class OpenAiService {

    @Inject
    LLMConfig llmConfig;
    private HttpClient httpClient;

    @PostConstruct
    void init() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)                
                .build();
    }

    public List<String> listModels() throws Exception {
        var endpoint = URI.create(getBaseUrl() + "/models");

        var requestBuilder = HttpRequest.newBuilder()
                .uri(endpoint)
                .header("Accept", "application/json")                
                .header("Cache-Control", "no-cache");

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
