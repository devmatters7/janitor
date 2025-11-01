package com.maintenance.ui.views.reports;

import com.maintenance.entity.Ticket;
import com.maintenance.entity.enums.TicketStatus;
import com.maintenance.service.TicketService;
import com.maintenance.ui.components.TicketChart;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Route(value = "reports", layout = com.maintenance.ui.MainLayout.class)
@PageTitle("Reports | Building Maintenance System")
@PermitAll
public class ReportsView extends VerticalLayout {
    
    private final TicketService ticketService;
    
    private final DatePicker startDatePicker;
    private final DatePicker endDatePicker;
    private final Button generateButton;
    
    private final TicketChart statusChart;
    private final TicketChart priorityChart;
    private final TicketChart monthlyChart;
    
    private final Grid<Ticket> ticketGrid;
    
    public ReportsView(TicketService ticketService) {
        this.ticketService = ticketService;
        
        addClassName("reports-view");
        setSpacing(true);
        setPadding(true);
        setSizeFull();
        
        // Add title
        H2 title = new H2("Maintenance Reports");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        add(title);
        
        // Create filter section
        createFilterSection();
        
        // Create charts section
        createChartsSection();
        
        // Create tickets table
        createTicketsTable();
        
        // Load initial data
        loadReportsData();
    }
    
    private void createFilterSection() {
        HorizontalLayout filterLayout = new HorizontalLayout();
        filterLayout.setWidthFull();
        filterLayout.setAlignItems(Alignment.BASELINE);
        filterLayout.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        
        // Date range filters
        startDatePicker = new DatePicker("Start Date");
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        
        endDatePicker = new DatePicker("End Date");
        endDatePicker.setValue(LocalDate.now());
        
        // Generate button
        generateButton = new Button("Generate Report", VaadinIcon.REFRESH.create());
        generateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        generateButton.addClickListener(event -> loadReportsData());
        
        filterLayout.add(startDatePicker, endDatePicker, generateButton);
        add(filterLayout);
    }
    
    private void createChartsSection() {
        HorizontalLayout chartsLayout = new HorizontalLayout();
        chartsLayout.setWidthFull();
        chartsLayout.setSpacing(true);
        chartsLayout.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        
        // Status chart
        statusChart = new TicketChart("Tickets by Status", TicketChart.ChartType.PIE);
        statusChart.setWidth("33%");
        chartsLayout.add(statusChart);
        
        // Priority chart
        priorityChart = new TicketChart("Tickets by Priority", TicketChart.ChartType.BAR);
        priorityChart.setWidth("33%");
        chartsLayout.add(priorityChart);
        
        // Monthly chart
        monthlyChart = new TicketChart("Monthly Tickets", TicketChart.ChartType.COLUMN);
        monthlyChart.setWidth("34%");
        chartsLayout.add(monthlyChart);
        
        add(chartsLayout);
    }
    
    private void createTicketsTable() {
        ticketGrid = new Grid<>(Ticket.class, false);
        ticketGrid.setWidthFull();
        ticketGrid.setHeight("400px");
        
        // Configure columns
        ticketGrid.addColumn(Ticket::getTitle)
                .setHeader("Title")
                .setAutoWidth(true)
                .setFlexGrow(1);
        
        ticketGrid.addColumn(ticket -> ticket.getStatus().name())
                .setHeader("Status")
                .setAutoWidth(true);
        
        ticketGrid.addColumn(ticket -> ticket.getPriority().name())
                .setHeader("Priority")
                .setAutoWidth(true);
        
        ticketGrid.addColumn(ticket -> ticket.getCategory().getName())
                .setHeader("Category")
                .setAutoWidth(true);
        
        ticketGrid.addColumn(ticket -> ticket.getReporter().getFullName())
                .setHeader("Reporter")
                .setAutoWidth(true);
        
        ticketGrid.addColumn(ticket -> {
                    if (ticket.getAssignee() != null) {
                        return ticket.getAssignee().getFullName();
                    }
                    return "Unassigned";
                })
                .setHeader("Assignee")
                .setAutoWidth(true);
        
        ticketGrid.addColumn(Ticket::getCreatedAt)
                .setHeader("Created")
                .setAutoWidth(true);
        
        add(ticketGrid);
    }
    
    private void loadReportsData() {
        // Load statistics
        loadStatusChart();
        loadPriorityChart();
        loadMonthlyChart();
        
        // Load tickets
        loadTicketsTable();
    }
    
    private void loadStatusChart() {
        Map<String, Long> statusData = ticketService.getTicketCountByStatus();
        statusChart.setData(statusData);
    }
    
    private void loadPriorityChart() {
        Map<String, Long> priorityData = ticketService.getTicketCountByPriority();
        priorityChart.setData(priorityData);
    }
    
    private void loadMonthlyChart() {
        Map<String, Long> monthlyData = ticketService.getMonthlyTicketCount(12);
        monthlyChart.setData(monthlyData);
    }
    
    private void loadTicketsTable() {
        List<Ticket> tickets = ticketService.findAllTickets();
        
        // Filter by date range if specified
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate != null && endDate != null) {
            tickets = tickets.stream()
                    .filter(ticket -> {
                        LocalDate ticketDate = ticket.getCreatedAt().toLocalDate();
                        return !ticketDate.isBefore(startDate) && !ticketDate.isAfter(endDate);
                    })
                    .toList();
        }
        
        ticketGrid.setItems(tickets);
    }
}