package org.fxapps.llmfx.services;

import java.util.List;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.fxapps.llmfx.config.LLMConfig;

import io.quarkus.rest.client.reactive.Url;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@ApplicationScoped
public class OpenAiService {

    @Inject
    LLMConfig llmConfig;

    @RegisterRestClient(baseUri = "notused")
    @RegisterClientHeaders(BearerTokenHeaderFactory.class)
    public interface OpenAiServiceRest {
        @GET
        @Path("/models")
        ModelListResponse listModels(@Url String url);
    }

    public record ModelListResponse(String object, List<ModelInfo> data) {}
    public record ModelInfo(String id, String object, long created, String owned_by) {}
    
    @Inject
    @RestClient
    OpenAiServiceRest openAiServiceRest;

    public List<String> listModels() {
        var models = openAiServiceRest.listModels(getBaseUrl());
        return models.data().stream().map(ModelInfo::id).toList();
    }

    public String getBaseUrl() {
        var baseUrl = llmConfig.url();
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    
}
