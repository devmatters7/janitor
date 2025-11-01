package com.maintenance.ui.components;

import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.Map;

public class TicketChart extends VerticalLayout {
    
    public enum ChartType {
        PIE, BAR, COLUMN, LINE
    }
    
    private final Chart chart;
    private final H3 title;
    
    public TicketChart(String chartTitle, ChartType type) {
        setPadding(false);
        setSpacing(false);
        
        // Create title
        title = new H3(chartTitle);
        title.addClassNames(
                LumoUtility.Margin.NONE,
                LumoUtility.Margin.Bottom.MEDIUM,
                LumoUtility.FontSize.LARGE,
                LumoUtility.FontWeight.MEDIUM
        );
        
        // Create chart
        chart = new Chart();
        chart.setHeight("300px");
        
        // Configure chart based on type
        switch (type) {
            case PIE:
                configurePieChart();
                break;
            case BAR:
                configureBarChart();
                break;
            case COLUMN:
                configureColumnChart();
                break;
            case LINE:
                configureLineChart();
                break;
        }
        
        add(title, chart);
    }
    
    private void configurePieChart() {
        chart.getConfiguration().setTitle("");
        chart.getConfiguration().getChart().setType(ChartType.PIE);
        
        PlotOptionsPie plotOptions = new PlotOptionsPie();
        plotOptions.setAllowPointSelect(true);
        plotOptions.setCursor(Cursor.POINTER);
        plotOptions.setShowInLegend(true);
        
        DataLabels dataLabels = new DataLabels();
        dataLabels.setEnabled(true);
        dataLabels.setFormat("<b>{point.name}</b>: {point.percentage:.1f} %");
        plotOptions.setDataLabels(dataLabels);
        
        chart.getConfiguration().setPlotOptions(plotOptions);
    }
    
    private void configureBarChart() {
        chart.getConfiguration().setTitle("");
        chart.getConfiguration().getChart().setType(ChartType.BAR);
        
        XAxis xAxis = new XAxis();
        xAxis.setType(AxisType.CATEGORY);
        chart.getConfiguration().addxAxis(xAxis);
        
        YAxis yAxis = new YAxis();
        yAxis.setMin(0);
        yAxis.setTitle("Count");
        chart.getConfiguration().addyAxis(yAxis);
        
        Legend legend = new Legend();
        legend.setEnabled(false);
        chart.getConfiguration().setLegend(legend);
        
        PlotOptionsBar plotOptions = new PlotOptionsBar();
        DataLabels dataLabels = new DataLabels();
        dataLabels.setEnabled(true);
        plotOptions.setDataLabels(dataLabels);
        chart.getConfiguration().setPlotOptions(plotOptions);
    }
    
    private void configureColumnChart() {
        chart.getConfiguration().setTitle("");
        chart.getConfiguration().getChart().setType(ChartType.COLUMN);
        
        XAxis xAxis = new XAxis();
        xAxis.setType(AxisType.CATEGORY);
        chart.getConfiguration().addxAxis(xAxis);
        
        YAxis yAxis = new YAxis();
        yAxis.setMin(0);
        yAxis.setTitle("Count");
        chart.getConfiguration().addyAxis(yAxis);
        
        Legend legend = new Legend();
        legend.setEnabled(false);
        chart.getConfiguration().setLegend(legend);
        
        PlotOptionsColumn plotOptions = new PlotOptionsColumn();
        DataLabels dataLabels = new DataLabels();
        dataLabels.setEnabled(true);
        plotOptions.setDataLabels(dataLabels);
        chart.getConfiguration().setPlotOptions(plotOptions);
    }
    
    private void configureLineChart() {
        chart.getConfiguration().setTitle("");
        chart.getConfiguration().getChart().setType(ChartType.LINE);
        
        XAxis xAxis = new XAxis();
        xAxis.setType(AxisType.CATEGORY);
        chart.getConfiguration().addxAxis(xAxis);
        
        YAxis yAxis = new YAxis();
        yAxis.setMin(0);
        yAxis.setTitle("Count");
        chart.getConfiguration().addyAxis(yAxis);
        
        Legend legend = new Legend();
        legend.setEnabled(false);
        chart.getConfiguration().setLegend(legend);
        
        PlotOptionsLine plotOptions = new PlotOptionsLine();
        DataLabels dataLabels = new DataLabels();
        dataLabels.setEnabled(true);
        plotOptions.setDataLabels(dataLabels);
        chart.getConfiguration().setPlotOptions(plotOptions);
    }
    
    public void setData(Map<String, Long> data) {
        Configuration configuration = chart.getConfiguration();
        configuration.setSeries();
        
        if (configuration.getChart().getType() == ChartType.PIE) {
            DataSeries series = new DataSeries();
            for (Map.Entry<String, Long> entry : data.entrySet()) {
                series.add(new DataSeriesItem(entry.getKey(), entry.getValue()));
            }
            configuration.addSeries(series);
        } else {
            ListSeries series = new ListSeries();
            series.setName("Tickets");
            
            String[] categories = new String[data.size()];
            Number[] values = new Number[data.size()];
            
            int i = 0;
            for (Map.Entry<String, Long> entry : data.entrySet()) {
                categories[i] = entry.getKey();
                values[i] = entry.getValue();
                i++;
            }
            
            configuration.getxAxis().setCategories(categories);
            series.setData(values);
            configuration.addSeries(series);
        }
        
        chart.drawChart();
    }
}