package org.fxapps.llmfx.services;

import java.util.List;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import org.fxapps.llmfx.config.RuntimeLLMConfig;
import org.jboss.logging.Logger;

import io.quarkus.rest.client.reactive.Url;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@ApplicationScoped
public class OpenAiService {

    @Inject
    RuntimeLLMConfig llmConfig;

    Logger logger = Logger.getLogger(OpenAiService.class);

    @RegisterRestClient(baseUri = "notused")
    @RegisterClientHeaders(BearerTokenHeaderFactory.class)
    public interface OpenAiServiceRest {
        @GET
        @Path("/models")
        ModelListResponse listModels(@Url String url);

        @POST
        @Path("/models/unload")
        void unloadModel(@Url String url, String model);
    }

    public record ModelListResponse(String object, List<ModelInfo> data) {
    }

    public record ModelInfo(String id, String model, String object, long created, String owned_by) {
    }

    @Inject
    @RestClient
    OpenAiServiceRest openAiServiceRest;

    public List<String> listModels() {
        var models = openAiServiceRest.listModels(getBaseUrl());
        return models.data().stream().map(ModelInfo::id).toList();
    }

    public void unloadModel(String modelId) {
        var reqBody = """
                {"model": "%s"}
                    """.formatted(modelId);
        try {
            openAiServiceRest.unloadModel(getBaseUrl(), reqBody);
        } catch (Exception e) {
            logger.warn("Error unloading model: " + modelId);
            logger.debug(e);            
        }
    }

    public String getBaseUrl() {
        var baseUrl = llmConfig.url();
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

}
