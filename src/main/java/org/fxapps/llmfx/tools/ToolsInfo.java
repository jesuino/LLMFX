package org.fxapps.llmfx.tools;

import java.util.List;
import java.util.Map;

import org.fxapps.llmfx.tools.code.CommandsTool;
import org.fxapps.llmfx.tools.files.FilesReaderTool;
import org.fxapps.llmfx.tools.files.FilesWriterTool;
import org.fxapps.llmfx.tools.time.DateTimeTool;
import org.fxapps.llmfx.tools.web.WebSearchTool;

import jakarta.inject.Singleton;

@Singleton
public class ToolsInfo {

    private static final String FILE_WRITE = "File Write";
    private static final String FILES_READ = "Files Read";
    private static final String WEB_SEARCH = "Web Search";
    private static final String DATE_TIME = "Date and Time";
    private static final String COMMANDS = "Commands";

    Map<String, Object> toolsMap = Map.of(
            FILES_READ, new FilesReaderTool(),
            FILE_WRITE, new FilesWriterTool(),
            WEB_SEARCH, new WebSearchTool(),
            DATE_TIME, new DateTimeTool(),
            COMMANDS, new CommandsTool());

    Map<String, List<String>> toolsCategoryMap = Map.of(
            "Files", List.of(FILES_READ, FILE_WRITE),
            "Web", List.of(WEB_SEARCH),
            "Date and Time", List.of(DATE_TIME),
            "Execute", List.of(COMMANDS));

    public Map<String, List<String>> getToolsCategoryMap() {
        return toolsCategoryMap;
    }

    public Map<String, Object> getToolsMap() {
        return toolsMap;
    }

}
