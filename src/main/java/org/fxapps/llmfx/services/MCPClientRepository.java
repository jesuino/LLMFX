package org.fxapps.llmfx.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.fxapps.llmfx.config.MCPCollections;
import org.fxapps.llmfx.config.MCPCollections.MCPServerDefinition;
import org.jboss.logging.Logger;

import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MCPClientRepository {

    @Inject
    MCPCollections mcpConfig;

    Logger logger = Logger.getLogger(MCPClientRepository.class);

    Map<String, McpClient> toolProviderRegister;

    @PostConstruct
    public void init() {
        this.toolProviderRegister = new HashMap<>();
        for (var mcpServer : mcpConfig.servers().entrySet()) {
            var def = mcpServer.getValue();
            var name = mcpServer.getKey();
            var transport = getTransport(def);
            try {
                var mcpClient = new DefaultMcpClient.Builder()
                        .transport(transport)
                        .build();
                toolProviderRegister.put(name, mcpClient);
            } catch (Exception e) {
                logger.error("Not able to load MCP server configuration for server " + name, e);
            }
        }
    }

    public Collection<String> mcpServers() {
        return toolProviderRegister.keySet();

    }

    public McpClient getMcpClient(String mcpName) {
        return toolProviderRegister.get(mcpName);
    }

    McpTransport getTransport(MCPServerDefinition def) {
        if (def.commands().isPresent() && !def.commands().isEmpty()) {
            return new StdioMcpTransport.Builder()
                    .command(def.commands().get())
                    .logEvents(true)
                    .build();
        }

        if (def.url().isPresent()) {
            return new HttpMcpTransport.Builder()
                    .sseUrl(def.url().get())
                    .build();
        }

        throw new IllegalArgumentException("The MCP server configuration must have a command or a URL");
    }

}
