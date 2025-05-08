package org.fxapps.llmfx.tools.graphics;

import static org.fxapps.llmfx.FXUtils.fixColor;

import java.util.Arrays;
import java.util.Map;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
@Singleton
public class JFXReportingTool {

    private static final String P_CHART_TITLE = "chart title";
    private static final String P_SERIES_NAME = "name of the series";
    private static final String P_HEIGHT = "height in pixels";
    private static final String P_WIDTH = "width in pixels";
    private static final String P_ROW = "row to place the item on the report grid.";
    private static final String P_COLUMN = "column to place the item on the report grid.";
    private static final String P_COLUMNS_SPAN = "number of columns the item should span";
    private static final String P_ROWS_SPAN = "number of rows the item should span";

    private GridPane gridPane;

    enum ChartType {
        AREA, LINE, BAR, SCATTER;
    }

    public void setGridPane(GridPane gridPane) {
        this.gridPane = gridPane;

    }

    @Tool("Creates a table for a report or dashboard. You can specify the number of columns and rows, dimensions, position, and data.")
    public void addTable(
            @P(P_COLUMN) int column,
            @P(P_ROW) int row,
            @P(P_COLUMNS_SPAN) int colSpan,
            @P(P_ROWS_SPAN) int rowspan,
            @P(P_WIDTH) int width,
            @P(P_HEIGHT) int height,
            @P("list of columns") String[] columnsNames,
            @P("data to be displayed.") String[][] data) {
        var tableView = new TableView<String[]>();

        tableView.setPrefWidth(width);
        tableView.setPrefHeight(height);

        ObservableList<String[]> tableData = FXCollections.observableArrayList();
        tableData.addAll(Arrays.asList(data));

        var columns = Arrays.stream(columnsNames).map(cl -> {
            var tableColumn = new TableColumn<String[], String>(cl);
            tableColumn.setCellValueFactory(cellData -> {
                var columnIndex = Arrays.asList(columnsNames).indexOf(cl);
                return new SimpleStringProperty(cellData.getValue()[columnIndex]);
            });
            return tableColumn;
        }).toList();

        tableView.getColumns().addAll(columns);
        tableView.setItems(tableData);
        add(tableView, column, row, colSpan, rowspan);
    }

    @Tool("Creates an XY chart for the report. You can specify the type, title, dimensions, position, series name, and data.")
    public void addXYChart(
            @P("XY chart type.") ChartType type,
            @P(P_CHART_TITLE) String title,
            @P(P_WIDTH) int width,
            @P(P_HEIGHT) int height,
            @P(P_COLUMN) int column,
            @P(P_ROW) int row,
            @P(P_COLUMNS_SPAN) int colSpan,
            @P(P_ROWS_SPAN) int rowspan,
            @P(P_SERIES_NAME) String seriesName,
            @P("X axis values") String[] categories,
            @P("y axis values") Float[] values) {

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        var chart = switch (type) {
            case AREA -> new AreaChart<>(xAxis, yAxis);
            case LINE -> new LineChart<>(xAxis, yAxis);
            case BAR -> new BarChart<>(xAxis, yAxis);
            case SCATTER -> new ScatterChart<>(xAxis, yAxis);
        };
        chart.setPrefWidth(width);
        chart.setPrefHeight(height);
        chart.setTitle(title);

        var seriesData = new XYChart.Series<String, Number>();
        seriesData.setName(seriesName);

        for (int i = 0; i < categories.length; i++) {
            seriesData.getData().add(new XYChart.Data<>(categories[i], values[i]));
        }

        chart.getData().add(seriesData);
        add(chart, column, row, colSpan, rowspan);        
    }


    @Tool("Creates a Bubble chart for the report. You can specify the title, dimensions, position, series name, and data.")
    public void addBubbleChart(            
            @P(P_CHART_TITLE) String title,
            @P(P_WIDTH) int width,
            @P(P_HEIGHT) int height,
            @P(P_COLUMN) int column,
            @P(P_ROW) int row,
            @P(P_COLUMNS_SPAN) int colSpan,
            @P(P_ROWS_SPAN) int rowspan,
            @P(P_SERIES_NAME) String seriesName,
            @P("X axis values") Double[] xValues,
            @P("y axis values") Double[] yValues,
            @P("bubble size values") Double[] bubbleValues) {

        var xAxis = new NumberAxis();
        var yAxis = new NumberAxis();        
        var chart = new BubbleChart<>(xAxis, yAxis);
        chart.setPrefWidth(width);
        chart.setPrefHeight(height);
        chart.setTitle(title);

        var seriesData = new XYChart.Series<Number, Number>();
        seriesData.setName(seriesName);

        for (int i = 0; i < xValues.length; i++) {
            seriesData.getData().add(new BubbleChart.Data<>(xValues[i], yValues[i], bubbleValues[i]));
        }

        chart.getData().add(seriesData);
        add(chart, column, row, colSpan, rowspan);        
    }

    @Tool("Creates a pie chart for the report. You can create a Pie chart with the specified title and data. The chart will be placed at the specified coordinates.")
    public void addPieChart(
            @P(P_CHART_TITLE) String title,
            @P(P_WIDTH) int width,
            @P(P_HEIGHT) int height,
            @P(P_COLUMN) int column,
            @P(P_ROW) int row,
            @P(P_COLUMNS_SPAN) int colSpan,
            @P(P_ROWS_SPAN) int rowspan,
            @P("Pie Chart data in format of a map where the key is the category and the value is the value to plotted ") Map<String, Float> data) {

        var pieChart = new PieChart();
        pieChart.setPrefWidth(width);
        pieChart.setPrefHeight(height);
        pieChart.setTitle(title);

        data.forEach((k, v) -> {
            pieChart.getData().add(new PieChart.Data(k, v));
        });

        add(pieChart, column, row, colSpan, rowspan);
    }

    @Tool("Adds text to the report. You can specify the position, dimensions, content, style, and alignment.")
    public void addText(
            @P(P_COLUMN) int column,
            @P(P_ROW) int row,
            @P(P_COLUMNS_SPAN) int colSpan,
            @P(P_ROWS_SPAN) int rowspan,
            @P("text to be drawn") String text,
            @P("text wrapping width in pixels") int width,
            @P("text font value. Use free and open source fonts") String fontFamily,
            @P("Font size") int fontSize,
            @P("Font color in web format") String fontColor,
            @P("Background color in web format") String backgroundColor,
            @P("font Posture. Possible values are ITALIC or REGULAR ") String fontPosture,
            @P("font weight. Values start at 0 and goes to 100. The smaller the value, the thinner the font") int fontWeight,
            @P("text alignment. Possible values are LEFT, CENTER, RIGHT and JUSTIFY") String textAligment,
            @P("stroke color in web format") String strokeColor,
            @P("text stroke width ") int strokeWidth

    ) {
        Text textNode = new Text(text);
        textNode.setFont(Font.font(
                fontFamily,
                FontWeight.findByWeight(fontWeight),
                FontPosture.findByName(fontPosture),
                fontSize));
        textNode.setWrappingWidth(width);

        textNode.setFill(fixColor(fontColor));
        textNode.setStroke(fixColor(strokeColor));
        textNode.setStrokeWidth(strokeWidth);
        textNode.setTextAlignment(TextAlignment.valueOf(textAligment));
        var textContainer = new StackPane(textNode);
        textContainer.setBackground(Background.fill(fixColor(backgroundColor)));
        add(textNode, column, row, colSpan, rowspan);
    }

    @Tool("Clear the current report or dashboard by removing all elements previously added.")
    public void clear() {
        Platform.runLater(() -> gridPane.getChildren().clear());
        
    }

    private void add(Node node, int column, int row, int colSpan, int rowspan) {
        Platform.runLater(() -> this.gridPane.add(node, column, row, colSpan, rowspan));
    }

}
