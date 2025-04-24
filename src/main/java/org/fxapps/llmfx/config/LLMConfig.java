package org.fxapps.llmfx.config;

import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "llm")
public interface LLMConfig {

    @ConfigProperty(defaultValue = "http://localhost:11434/v1")
    String url();

    Optional<String> model();

    @ConfigProperty(defaultValue = "200")
    int timeout();

    @ConfigProperty(defaultValue = "")
    Optional<String> key();

    @ConfigProperty(defaultValue = "")
    Optional<String> systemMessage();

}
