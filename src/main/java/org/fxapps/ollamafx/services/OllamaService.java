package org.fxapps.ollamafx.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;

@ApplicationScoped
public class OllamaService {

    public List<String> listModels(String ollamaUrl) throws Exception {
        var httpClient = HttpClient.newHttpClient();
        var endpoint = URI.create(ollamaUrl + "/api/tags");
        var request = HttpRequest.newBuilder()
                .uri(endpoint)
                .header("Accepts", "application/json")
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        var json = Json.createReader(response.body()).read();
        return json.asJsonObject()
                .get("models")
                .asJsonArray()
                .stream()
                .map(j -> j.asJsonObject().getString("model"))
                .toList();

    }

}
