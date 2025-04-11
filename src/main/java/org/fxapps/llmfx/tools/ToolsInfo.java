package org.fxapps.llmfx.tools;

import java.util.List;
import java.util.Map;

import jakarta.inject.Singleton;

@Singleton
public class ToolsInfo {

    private static final String FILE_WRITE = "File Write";

    private static final String FILES_READ = "Files Read";

    Map<String, Object> toolsMap = Map.of(
            FILES_READ, new FilesReaderTool(),
            FILE_WRITE, new FilesWriterTool());

    Map<String, List<String>> toolsCategoryMap = Map.of(
            "Files", List.of(FILES_READ, FILE_WRITE));

    public Map<String, List<String>> getToolsCategoryMap() {
        return toolsCategoryMap;
    }

    public Map<String, Object> getToolsMap() {
        return toolsMap;
    }

}
