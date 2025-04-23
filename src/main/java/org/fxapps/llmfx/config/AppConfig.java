package org.fxapps.llmfx.config;

import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "app")
public interface AppConfig {

    @ConfigProperty(defaultValue = "true")    
    Optional<Boolean> alwaysOnTop();

    @ConfigProperty
    Optional<String> historyFile();
    
}
