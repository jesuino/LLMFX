package org.fxapps.llmfx.services;

import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import org.fxapps.llmfx.config.LLMConfig;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

/**
 * adds the bearer token to the request headers
 */
public class BearerTokenHeaderFactory implements ClientHeadersFactory {

    @Inject
    LLMConfig llmConfig;
    
    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
        var result = new MultivaluedHashMap<String, String>();
        llmConfig.key().ifPresent(key -> result.add("Authorization", "Bearer " + key));
        return result;
    }
}
