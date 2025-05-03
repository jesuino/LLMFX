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
import org.fxapps.llmfx.tools.graphics.JFXReportingTool;
import org.fxapps.llmfx.tools.graphics.JFXShapesTool;
import org.fxapps.llmfx.tools.graphics.JFXWebRenderingTool;
import org.fxapps.llmfx.tools.time.DateTimeTool;
import org.fxapps.llmfx.tools.web.WebSearchTool;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class ToolsInfo {

    public static final String CANVAS_DRAWING = "Canvas Drawing";
    public static final String CANVAS_PIXELS = "Canvas Pixels";
    public static final String WEB_RENDER = "Web Render";
    public static final String REPORTING = "Reporting";
    public static final String FILE_WRITE = "File Write";
    public static final String FILES_READ = "Files Read";
    public static final String WEB_SEARCH = "Web Search";
    public static final String DATE_TIME = "Date and Time";
    public static final String COMMANDS = "Commands";
    public static final String PYTHON = "Python";
    public static final String _3D = "3D";
    public static final String SHAPES = "Shapes";

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
    private PythonTool pythonTool;
    @Inject
    private JFXCanvasTool drawingTool;
    @Inject
    private JFXReportingTool reportingTool;
    @Inject
    private JFXWebRenderingTool webTool;
    @Inject
    private JFX3dTool _3dTools;
    @Inject
    private JFXCanvasPixelTool canvasPixelTool;
    @Inject
    private JFXShapesTool shapesTool;

    Map<String, Object> toolsMap;

    // TODO: Create a 3d tool to draw 3d stuff

    Map<String, List<String>> toolsCategoryMap = Map.of(
            "Files", List.of(FILES_READ, FILE_WRITE),
            "Web", List.of(WEB_SEARCH),
            "Date and Time", List.of(DATE_TIME),
            "Execute", List.of(COMMANDS, PYTHON),
            "Graphics", List.of(CANVAS_DRAWING, REPORTING, WEB_RENDER, _3D, CANVAS_PIXELS, SHAPES));

    @PostConstruct
    void init() {

        toolsMap = new HashMap<>();

        // graphics
        toolsMap.putAll(Map.of(
                CANVAS_DRAWING, drawingTool,
                REPORTING, reportingTool,
                WEB_RENDER, webTool,
                _3D, _3dTools,
                CANVAS_PIXELS, canvasPixelTool,
                SHAPES, shapesTool));

        toolsMap.putAll(Map.of(
                FILES_READ, filesReaderTool,
                FILE_WRITE, filesWriterTool,
                WEB_SEARCH, webSearchTool,
                DATE_TIME, dateTimeTool,
                COMMANDS, commandsTool,
                PYTHON, pythonTool));

    }

    public Map<String, List<String>> getToolsCategoryMap() {
        return toolsCategoryMap;
    }

    public Map<String, Object> getToolsMap() {
        return toolsMap;
    }

}
