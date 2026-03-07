package org.fxapps.llmfx.windows;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.fxapps.llmfx.services.MCPClientRepository;
import org.fxapps.llmfx.services.MCPFunctionRegistry;
import org.jboss.logging.Logger;

import dev.langchain4j.agent.tool.ToolSpecification;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Dialog for selecting which functions to register from an MCP server.
 */
@Singleton
public class MCPFunctionSelectionDialog extends Dialog<Set<String>> {

    Logger logger = Logger.getLogger(MCPFunctionSelectionDialog.class);

    @Inject
    MCPClientRepository mcpClientRepository;

    @Inject
    MCPFunctionRegistry mcpFunctionRegistry;

    private VBox functionList;

    @PostConstruct
    private void setup() {
        // Create UI
        functionList = new VBox(10);
        functionList.setPadding(new Insets(10));

        // Add Select All / Deselect All buttons
        var buttonBar = new HBox(10);
        var selectAllBtn = new Button("Select All");
        var deselectAllBtn = new Button("Deselect All");

        selectAllBtn.setOnAction(e -> functionList.getChildren().stream()
                .forEach(n -> {
                    if (n instanceof CheckBox cb) {
                        cb.setSelected(true);
                    }
                }));

        deselectAllBtn.setOnAction(e -> functionList.getChildren().stream()
                .forEach(n -> {
                    if (n instanceof CheckBox cb) {
                        cb.setSelected(false);
                    }
                }));

        buttonBar.getChildren().addAll(selectAllBtn, deselectAllBtn);
        buttonBar.setPadding(new Insets(0, 0, 10, 0));

        var mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(10));
        mainContainer.getChildren().addAll(buttonBar, new Label("Available Functions:"), functionList);
        var scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setPrefWidth(500);

        getDialogPane().setContent(scrollPane);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Convert result to set of selected function names
        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return functionList.getChildren().stream()
                        .flatMap(node -> {
                            if (node instanceof CheckBox) {
                                return java.util.stream.Stream.of((CheckBox) node);
                            } else if (node instanceof VBox) {
                                return ((VBox) node).getChildren().stream()
                                        .filter(child -> child instanceof CheckBox)
                                        .map(child -> (CheckBox) child);
                            }
                            return java.util.stream.Stream.empty();
                        })
                        .filter(CheckBox::isSelected)
                        .map(CheckBox::getText)
                        .collect(Collectors.toSet());
            }
            return null;
        });

    }

    public void showForMcp(String mcpName) {
        setTitle("Select Functions - " + mcpName);
        setHeaderText("Choose which functions to register from " + mcpName);

        var mcpClient = mcpClientRepository.getMcpClient(mcpName);
        if (mcpClient == null) {
            logger.error("MCP client not found: " + mcpName);
            return;
        }

        // Get available tools from the MCP client
        List<ToolSpecification> tools;
        try {
            tools = mcpClient.listTools();
        } catch (Exception e) {
            logger.error("Error listing tools for MCP: " + mcpName, e);
            return;
        }

        var selectedFunctions = mcpFunctionRegistry.getSelectedFunctions(mcpName);

        if (!tools.isEmpty()) {

            // Add checkbox for each function
            functionList.getChildren().clear();
            for (ToolSpecification tool : tools) {

                var checkBox = new CheckBox(tool.name());

                // Add description as tooltip if available
                functionList.getChildren().add(checkBox);
                if (tool.description() != null && !tool.description().isEmpty()) {
                    var descLabel = new Label(" - " + tool.description());
                    descLabel.setWrapText(true);
                    descLabel.setMaxWidth(400);
                    descLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 0.9em;");
                    functionList.getChildren().add(descLabel);
                }

                checkBox.setSelected(selectedFunctions.contains(tool.name()));
                
            }

            showAndWait().ifPresent(selectedFuncs -> {
                mcpFunctionRegistry.setSelectedFunctions(mcpName, selectedFuncs);
            });

        }

    }
}
