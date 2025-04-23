package org.fxapps.llmfx.tools.jfx;

import java.util.Arrays;

import org.fxapps.llmfx.Events.NewReportingNodeEvent;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;

@Singleton
public class JFXReportingTool {

    @Inject
    Event<NewReportingNodeEvent> newReportingNodeEvent;

    @Tool("Creates or set a title for the report. You can create a title for the report with the specified text and font size.")
    public void title(
            @P("Reporting title text") String text,
            @P("Reporting title font size") int fontSize,
            @P("Reporting title text color in CSS format") String color) {

        Text title = new Text(text);
        title.setFont(javafx.scene.text.Font.font(fontSize));
        title.setFill(javafx.scene.paint.Color.valueOf(color));

        newReportingNodeEvent.fire(new NewReportingNodeEvent(title, 0, 0));
    }

    @Tool("""
            Creates a table for the report. You can create a table with the specified number of columns and rows.
            The table will be placed at the specified coordinates.
            Make sure that the columns length matches the number of columns in the data.
            """)
    public void addTable(
            @P("The column to place the table") int column,
            @P("The column to place the table") int row,
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

        newReportingNodeEvent.fire(new NewReportingNodeEvent(tableView, column, row));

    }

    @Tool("Creates a bar chart for the report. You can create a bar chart with the specified title and data. The chart will be placed at the specified coordinates.")
    public void addBarChart(
            @P("The Chart title") String title,
            @P("The chart width in pixels") int width,
            @P("The chart height in pixels") int height,
            @P("The column to place the bar chart") int column,
            @P("The row to place the bar chart") int row,
            @P("The name of the series") String seriesName,
            @P("The X axis values") String[] categories,
            @P("The y axis values") Float[] values) {

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        var barchart = new BarChart<>(xAxis, yAxis);
        barchart.setPrefWidth(width);
        barchart.setPrefHeight(height);
        barchart.setTitle(title);

        var seriesData = new XYChart.Series<String, Number>();
        seriesData.setName(seriesName);

        for (int i = 0; i < categories.length; i++) {
            seriesData.getData().add(new XYChart.Data<>(categories[i], values[i]));
        }

        barchart.getData().add(seriesData);
        newReportingNodeEvent.fire(new NewReportingNodeEvent(barchart, column, row));

    }

}
