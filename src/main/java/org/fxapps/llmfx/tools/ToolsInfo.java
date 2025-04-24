package org.fxapps.llmfx.tools;

import java.util.List;
import java.util.Map;

import org.fxapps.llmfx.tools.code.CommandsTool;
import org.fxapps.llmfx.tools.files.FilesReaderTool;
import org.fxapps.llmfx.tools.files.FilesWriterTool;
import org.fxapps.llmfx.tools.graphics.JFXDrawerTool;
import org.fxapps.llmfx.tools.graphics.JFXPathTool;
import org.fxapps.llmfx.tools.graphics.JFXReportingTool;
import org.fxapps.llmfx.tools.graphics.JFXWebRenderingTool;
import org.fxapps.llmfx.tools.time.DateTimeTool;
import org.fxapps.llmfx.tools.web.WebSearchTool;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ToolsInfo {

    private static final String CANVAS_DRAWING = "Canvas Drawing";
    private static final String PATH_DRAWING = "Path Drawing";
    private static final String WEB_RENDER = "Web Render";
    private static final String REPORTING = "Reporting";
    private static final String FILE_WRITE = "File Write";
    private static final String FILES_READ = "Files Read";
    private static final String WEB_SEARCH = "Web Search";
    private static final String DATE_TIME = "Date and Time";
    private static final String COMMANDS = "Commands";

    @Inject
    private FilesReaderTool filesReaderTool;
    @Inject
    private FilesWriterTool filesWriterTool;
    @Inject
    private WebSearchTool webSearchTool;
    @Inject
    private DateTimeTool dateTimeTool;
    @Inject
    private CommandsTool commandsTool;
    @Inject
    private JFXDrawerTool drawerTool;
    @Inject
    private JFXPathTool pathTool;
    @Inject
    private JFXReportingTool reportingTool;
    @Inject
    private JFXWebRenderingTool webTool;

    Map<String, Object> toolsMap;

    // TODO: Create a 3d tool to draw 3d stuff

    Map<String, List<String>> toolsCategoryMap = Map.of(
            "Files", List.of(FILES_READ, FILE_WRITE),
            "Web", List.of(WEB_SEARCH),
            "Date and Time", List.of(DATE_TIME),
            "Execute", List.of(COMMANDS),
            "Graphics", List.of(PATH_DRAWING, CANVAS_DRAWING, REPORTING, WEB_RENDER));

    @PostConstruct
    void init() {

        toolsMap = Map.of(
                FILES_READ, filesReaderTool,
                FILE_WRITE, filesWriterTool,
                WEB_SEARCH, webSearchTool,
                DATE_TIME, dateTimeTool,
                COMMANDS, commandsTool,
                CANVAS_DRAWING, drawerTool,
                REPORTING, reportingTool,
                PATH_DRAWING, pathTool,
                WEB_RENDER, webTool);

    }

    public Map<String, List<String>> getToolsCategoryMap() {
        return toolsCategoryMap;
    }

    public Map<String, Object> getToolsMap() {
        return toolsMap;
    }

}
