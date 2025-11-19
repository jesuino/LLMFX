package org.fxapps.llmfx.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fxapps.llmfx.tools.code.CommandsTool;
import org.fxapps.llmfx.tools.code.PythonTool;
import org.fxapps.llmfx.tools.files.FilesReaderTool;
import org.fxapps.llmfx.tools.files.FilesWriterTool;
import org.fxapps.llmfx.tools.graphics.JFX3dTool;
import org.fxapps.llmfx.tools.graphics.JFXCanvasPixelTool;
import org.fxapps.llmfx.tools.graphics.JFXCanvasTool;
import org.fxapps.llmfx.tools.graphics.JFXDrawerTool;
import org.fxapps.llmfx.tools.graphics.JFXReportingTool;
import org.fxapps.llmfx.tools.graphics.JFXShapesTool;
import org.fxapps.llmfx.tools.graphics.JFXWebRenderingTool;
import org.fxapps.llmfx.tools.system.ClipboardTool;
import org.fxapps.llmfx.tools.time.DateTimeTool;
import org.fxapps.llmfx.tools.web.WebOpenTool;
import org.fxapps.llmfx.tools.web.WebSearchTool;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ToolsInfo {

    public static final String CANVAS_DRAWING = "Canvas Drawing";
    public static final String CANVAS_PIXELS = "Canvas Pixels";
    public static final String WEB_RENDER = "Web Render";
    public static final String WEB_OPEN = "Open Web URL";
    public static final String REPORTING = "Reporting";
    public static final String FILE_WRITE = "File Write";
    public static final String FILES_READ = "Files Read";
    public static final String WEB_SEARCH = "Web Search";
    public static final String DATE_TIME = "Date and Time";
    public static final String COMMANDS = "Commands";
    public static final String PYTHON = "Python";
    public static final String _3D = "3D";
    public static final String SHAPES = "Shapes";
    public static final String DRAWER = "Drawer";
    public static final String CLIPBOARD = "Clipboard";

    @Inject
    private FilesReaderTool filesReaderTool;
    @Inject
    private FilesWriterTool filesWriterTool;
    @Inject
    private WebSearchTool webSearchTool;
    @Inject
    private WebOpenTool webOpenTool;
    @Inject
    private DateTimeTool dateTimeTool;
    @Inject
    private CommandsTool commandsTool;
    @Inject
    private PythonTool pythonTool;
    @Inject
    private JFXCanvasTool drawingTool;
    @Inject
    private JFXReportingTool reportingTool;
    @Inject
    private JFXWebRenderingTool webRenderingTool;
    @Inject
    private JFX3dTool _3dTools;
    @Inject
    private JFXCanvasPixelTool canvasPixelTool;
    @Inject
    private JFXShapesTool shapesTool;
    @Inject
    private JFXDrawerTool drawerTool;
    @Inject
    private ClipboardTool clipboardTool;

    Map<String, Object> toolsMap;

    Map<String, List<String>> toolsCategoryMap = Map.of(
            "Files", List.of(FILES_READ, FILE_WRITE),
            "Web", List.of(WEB_SEARCH, WEB_OPEN),
            "Date and Time", List.of(DATE_TIME),
            "Execute", List.of(COMMANDS, PYTHON),
            "System", List.of(CLIPBOARD),
            "Graphics", List.of(DRAWER, REPORTING, WEB_RENDER, _3D, SHAPES));

    @PostConstruct
    void init() {

        toolsMap = new HashMap<>();

        // graphics
        toolsMap.putAll(Map.of(
                DRAWER, drawerTool,                
                REPORTING, reportingTool,
                _3D, _3dTools,
                WEB_RENDER, webRenderingTool));
        // Web
        toolsMap.putAll(Map.of(
                WEB_SEARCH, webSearchTool,
                WEB_OPEN, webOpenTool));

        toolsMap.putAll(Map.of(
                FILES_READ, filesReaderTool,
                FILE_WRITE, filesWriterTool,
                DATE_TIME, dateTimeTool,
                COMMANDS, commandsTool,
                PYTHON, pythonTool,
                CLIPBOARD, clipboardTool));

    }

    public Map<String, List<String>> getToolsCategoryMap() {
        return toolsCategoryMap;
    }

    public Map<String, Object> getToolsMap() {
        return toolsMap;
    }

}
