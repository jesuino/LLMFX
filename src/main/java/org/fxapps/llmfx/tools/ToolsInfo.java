package org.fxapps.llmfx.tools;

import java.util.List;
import java.util.Map;

import org.fxapps.llmfx.tools.code.CommandsTool;
import org.fxapps.llmfx.tools.files.FilesReaderTool;
import org.fxapps.llmfx.tools.files.FilesWriterTool;
import org.fxapps.llmfx.tools.jfx.JFXDrawerTool;
import org.fxapps.llmfx.tools.jfx.JFXReportingTool;
import org.fxapps.llmfx.tools.time.DateTimeTool;
import org.fxapps.llmfx.tools.web.WebSearchTool;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ToolsInfo {

    private static final String CANVAS_DRAWING = "Canvas Drawing";
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
    private JFXReportingTool reportingTool;

    Map<String, Object> toolsMap;

    Map<String, List<String>> toolsCategoryMap = Map.of(
            "Files", List.of(FILES_READ, FILE_WRITE),
            "Web", List.of(WEB_SEARCH),
            "Date and Time", List.of(DATE_TIME),
            "Execute", List.of(COMMANDS),
            "JFX", List.of(CANVAS_DRAWING, REPORTING));

    @PostConstruct
    void init() {

        toolsMap = Map.of(
                FILES_READ, filesReaderTool,
                FILE_WRITE, filesWriterTool,
                WEB_SEARCH, webSearchTool,
                DATE_TIME, dateTimeTool,
                COMMANDS, commandsTool,
                CANVAS_DRAWING, drawerTool,
                REPORTING, reportingTool);

    }

    public Map<String, List<String>> getToolsCategoryMap() {
        return toolsCategoryMap;
    }

    public Map<String, Object> getToolsMap() {
        return toolsMap;
    }

}
