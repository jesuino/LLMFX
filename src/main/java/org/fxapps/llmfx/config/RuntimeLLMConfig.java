package org.fxapps.llmfx.config;

import java.util.Optional;

import org.fxapps.llmfx.windows.ConnectionConfigDialog;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class RuntimeLLMConfig {

    @Inject
    LLMConfig llmConfig;

    private String url;
    private String key;
    private String model;

    public String url() {
        return url != null ? url : llmConfig.url();
    }

    public Optional<String> key() {
        return key != null ? Optional.of(key) : llmConfig.key();
    }

    public Optional<String> model() {
        return model != null ? Optional.of(model) : llmConfig.model();
    }

    public void fromConnectionConfig(ConnectionConfigDialog.ConnectionConfig config) {
        this.url = config.url();
        this.key = config.key();
        this.model = config.model();
    }
}
