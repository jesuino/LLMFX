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


    private GridPane gridPane;

    enum ChartType {
        AREA, LINE, BAR, SCATTER;
    }

    public void setGridPane(GridPane gridPane) {
        this.gridPane = gridPane;

    }

    @Tool("""
            Creates a table for a report or dashboard. You can create a table with the specified number of columns and rows.
            The table will be placed at the specified coordinates.
            Make sure that the columns length matches the number of columns in the data.
            """)
    public void addTable(
            @P("The column to place the table on the report grid") int column,
            @P("The row to place the table on the report grid") int row,
            @P("The number of columns the table should span") int colSpan,
            @P("the number of rows the table should span") int rowspan,
            @P("Table width in pixels") int width,
            @P("Table height in pixels") int height,
            @P("The list of columns") String[] columnsNames,
            @P("The data to be displayed.") String[][] data) {
        var tableView = new TableView<String[]>();

        tableView.setPrefWidth(width);
        tableView.setPrefHeight(height);

        ObservableList<String[]> tableData = FXCollections.observableArrayList();
        for (String[] rowData : data) {
            tableData.add(rowData);
        }

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



    @Tool("Creates a XY chart for the report. You can create a chart with the specified title and data. The chart will be placed at the specified coordinates.")
    public void addXYChart(
            @P("The XY chart type.") ChartType type,
            @P("The Chart title") String title,
            @P("The chart width in pixels") int width,
            @P("The chart height in pixels") int height,
            @P("The column to place the chart on the report grid.") int column,
            @P("The row to place the chart on the report grid.") int row,
            @P("The number of columns the chart should span") int colSpan,
            @P("the number of rows the chart should span") int rowspan,
            @P("The name of the series") String seriesName,
            @P("The X axis values") String[] categories,
            @P("The y axis values") Float[] values) {

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


    @Tool("Creates a Bubble chart for the report. You can create a chart with the specified title and data. The chart will be placed at the specified coordinates.")
    public void addBubbleChart(            
            @P("The Chart title") String title,
            @P("The chart width in pixels") int width,
            @P("The chart height in pixels") int height,
            @P("The column to place the chart on the report grid.") int column,
            @P("The row to place the chart on the report grid.") int row,
            @P("The number of columns the chart should span") int colSpan,
            @P("the number of rows the chart should span") int rowspan,
            @P("The name of the series") String seriesName,
            @P("The X axis values") Double[] xValues,
            @P("The y axis values") Double[] yValues,
            @P("The bubble size values. It determines the bubble size") Double[] bubbleValues) {

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
            @P("The Chart title") String title,
            @P("The chart width in pixels") int width,
            @P("The chart height in pixels") int height,
            @P("The column to place the chart on the report grid.") int column,
            @P("The row to place the chart on the report grid. ") int row,
            @P("The number of columns the chart should span") int colSpan,
            @P("the number of rows the chart should span") int rowspan,
            @P("The Pie Chart data in format of a map where the key is the category and the value is the value to plotted ") Map<String, Float> data) {

        var pieChart = new PieChart();
        pieChart.setPrefWidth(width);
        pieChart.setPrefHeight(height);
        pieChart.setTitle(title);

        data.forEach((k, v) -> {
            pieChart.getData().add(new PieChart.Data(k, v));
        });

        add(pieChart, column, row, colSpan, rowspan);
    }

    @Tool("Creates a text chart for the report. The text will be placed at the specified coordinates. Use this for titles, insights or explanation.")
    public void addText(
            @P("The column to place the text on the report grid.") int column,
            @P("The row to place the text on the report grid. ") int row,
            @P("The number of columns the text should span") int colSpan,
            @P("the number of rows the text should span") int rowspan,
            @P("The text to be drawn") String text,
            @P("The text wrapping width in pixels") int width,
            @P("The text font value. Use free and open source fonts") String fontFamily,
            @P("Font size") int fontSize,
            @P("Font color in web format") String fontColor,
            @P("Background color in web format") String backgroundColor,
            @P("The font Posture. Possible values are ITALIC or REGULAR ") String fontPosture,
            @P("The font weight. Values start at 0 and goes to 100. The smaller the value, the thinner the font") int fontWeight,
            @P("The text alignment. Possible values are LEFT, CENTER, RIGHT and JUSTIFY") String textAligment,
            @P("Stroke color in web format") String strokeColor,
            @P("The text stroke width ") int strokeWidth

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
