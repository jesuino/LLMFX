package org.fxapps.llmfx.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Singleton;

/**
 * Registry to store which functions are selected for each MCP server.
 * When no functions are explicitly selected for an MCP, all functions are
 * available.
 */
@Singleton
public class MCPFunctionRegistry {

    private final Map<String, Set<String>> selectedFunctions = new HashMap<>();

    /**
     * Set the selected functions for a specific MCP server.
     *
     * @param mcpName       The name of the MCP server
     * @param functionNames The set of selected function names
     */
    public void setSelectedFunctions(String mcpName, Set<String> functionNames) {
        if (functionNames == null || functionNames.isEmpty()) {
            selectedFunctions.remove(mcpName);
        } else {
            selectedFunctions.put(mcpName, new HashSet<>(functionNames));
        }
    }

    /**
     * Get the selected functions for a specific MCP server.
     * Returns an empty set if no functions have been explicitly selected (meaning
     * all are available).
     *
     * @param mcpName The name of the MCP server
     * @return Set of selected function names, or empty set if all functions should
     *         be used
     */
    public Set<String> getSelectedFunctions(String mcpName) {
        return selectedFunctions.getOrDefault(mcpName, new HashSet<>());
    }

    /**
     * Check if a specific function is selected for an MCP server.
     * Returns true if no functions are explicitly selected (all available) or if
     * the function is in the selected set.
     *
     * @param mcpName      The name of the MCP server
     * @param functionName The name of the function
     * @return true if the function should be available
     */
    public boolean isFunctionSelected(String mcpName, String functionName) {
        if (!selectedFunctions.containsKey(mcpName)) {
            return true; // No explicit selection means all functions are available
        }
        return selectedFunctions.get(mcpName).contains(functionName);
    }

    /**
     * Clear all selected functions for a specific MCP server.
     *
     * @param mcpName The name of the MCP server
     */
    public void clearSelectedFunctions(String mcpName) {
        selectedFunctions.remove(mcpName);
    }

}
