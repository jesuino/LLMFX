package org.fxapps.llmfx.tools;

import java.util.List;
import java.util.Map;

import org.fxapps.llmfx.tools.files.FilesReaderTool;
import org.fxapps.llmfx.tools.files.FilesWriterTool;
import org.fxapps.llmfx.tools.web.WebSearchTool;

import jakarta.inject.Singleton;

@Singleton
public class ToolsInfo {

    private static final String FILE_WRITE = "File Write";

    private static final String FILES_READ = "Files Read";
    private static final String WEB_SEARCH = "Web Search";

    Map<String, Object> toolsMap = Map.of(
            FILES_READ, new FilesReaderTool(),
            FILE_WRITE, new FilesWriterTool(),
            WEB_SEARCH, new WebSearchTool());

    Map<String, List<String>> toolsCategoryMap = Map.of(
            "Files", List.of(FILES_READ, FILE_WRITE),
            "Web", List.of(WEB_SEARCH)
            );

    public Map<String, List<String>> getToolsCategoryMap() {
        return toolsCategoryMap;
    }

    public Map<String, Object> getToolsMap() {
        return toolsMap;
    }

}
