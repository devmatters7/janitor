package com.maintenance.ui.views.dashboard;

import com.maintenance.entity.Ticket;
import com.maintenance.entity.User;
import com.maintenance.entity.enums.Priority;
import com.maintenance.entity.enums.TicketStatus;
import com.maintenance.security.SecurityService;
import com.maintenance.service.TicketService;
import com.maintenance.service.UserService;
import com.maintenance.ui.components.StatisticsCard;
import com.maintenance.ui.components.TicketChart;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDateTime;
import java.util.List;

@Route(value = "dashboard", layout = com.maintenance.ui.MainLayout.class)
@RouteAlias(value = "", layout = com.maintenance.ui.MainLayout.class)
@PageTitle("Dashboard | Building Maintenance System")
@PermitAll
public class DashboardView extends VerticalLayout {
    
    private final TicketService ticketService;
    private final SecurityService securityService;
    private final UserService userService;
    
    public DashboardView(TicketService ticketService, SecurityService securityService, UserService userService) {
        this.ticketService = ticketService;
        this.securityService = securityService;
        this.userService = userService;
        
        addClassName("dashboard-view");
        setSpacing(true);
        setPadding(true);
        setSizeFull();
        
        // Add content based on user role
        User currentUser = securityService.getAuthenticatedUser().orElse(null);
        
        if (currentUser != null && currentUser.getRole() == com.maintenance.entity.enums.Role.ADMIN) {
            createAdminDashboard();
        } else if (currentUser != null && currentUser.getRole() == com.maintenance.entity.enums.Role.TECHNICIAN) {
            createTechnicianDashboard();
        } else {
            createTenantDashboard();
        }
    }
    
    private void createAdminDashboard() {
        // Statistics cards
        HorizontalLayout statsLayout = createStatisticsCards();
        add(statsLayout);
        
        // Charts
        HorizontalLayout chartsLayout = createChartsLayout();
        add(chartsLayout);
        
        // Recent tickets and overdue tickets
        HorizontalLayout ticketsLayout = new HorizontalLayout();
        ticketsLayout.setWidthFull();
        ticketsLayout.setSpacing(true);
        
        ticketsLayout.add(createRecentTicketsGrid());
        ticketsLayout.add(createOverdueTicketsGrid());
        
        add(ticketsLayout);
    }
    
    private void createTechnicianDashboard() {
        // Statistics for technician
        HorizontalLayout statsLayout = createTechnicianStatisticsCards();
        add(statsLayout);
        
        // Assigned tickets
        add(createAssignedTicketsGrid());
        
        // Quick actions
        add(createQuickActionsPanel());
    }
    
    private void createTenantDashboard() {
        // Statistics for tenant
        HorizontalLayout statsLayout = createTenantStatisticsCards();
        add(statsLayout);
        
        // My tickets
        add(createMyTicketsGrid());
        
        // Quick actions
        add(createQuickActionsPanel());
    }
    
    private HorizontalLayout createStatisticsCards() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);
        
        // Total tickets
        long totalTickets = ticketService.countAllTickets();
        StatisticsCard totalCard = new StatisticsCard(
                "Total Tickets", 
                String.valueOf(totalTickets), 
                VaadinIcon.FILE_TEXT, 
                "primary"
        );
        layout.add(totalCard);
        
        // Open tickets
        long openTickets = ticketService.countTicketsByStatus(TicketStatus.OPEN);
        StatisticsCard openCard = new StatisticsCard(
                "Open Tickets", 
                String.valueOf(openTickets), 
                VaadinIcon.EXCLAMATION_CIRCLE, 
                "error"
        );
        layout.add(openCard);
        
        // In Progress tickets
        long inProgressTickets = ticketService.countTicketsByStatus(TicketStatus.IN_PROGRESS);
        StatisticsCard progressCard = new StatisticsCard(
                "In Progress", 
                String.valueOf(inProgressTickets), 
                VaadinIcon.COG, 
                "contrast"
        );
        layout.add(progressCard);
        
        // Overdue tickets
        long overdueTickets = ticketService.countOverdueTickets();
        StatisticsCard overdueCard = new StatisticsCard(
                "Overdue Tickets", 
                String.valueOf(overdueTickets), 
                VaadinIcon.CLOCK, 
                "error"
        );
        layout.add(overdueCard);
        
        return layout;
    }
    
    private HorizontalLayout createTechnicianStatisticsCards() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);
        
        User currentUser = securityService.getAuthenticatedUser().orElse(null);
        if (currentUser != null) {
            // Assigned tickets
            long assignedTickets = ticketService.countTicketsByAssignee(currentUser);
            StatisticsCard assignedCard = new StatisticsCard(
                    "Assigned to Me", 
                    String.valueOf(assignedTickets), 
                    VaadinIcon.USER_CHECK, 
                    "primary"
            );
            layout.add(assignedCard);
            
            // My open tickets
            long myOpenTickets = ticketService.countTicketsByAssigneeAndStatus(currentUser, TicketStatus.OPEN);
            StatisticsCard openCard = new StatisticsCard(
                    "My Open Tickets", 
                    String.valueOf(myOpenTickets), 
                    VaadinIcon.EXCLAMATION_CIRCLE, 
                    "error"
            );
            layout.add(openCard);
            
            // My in progress tickets
            long myProgressTickets = ticketService.countTicketsByAssigneeAndStatus(currentUser, TicketStatus.IN_PROGRESS);
            StatisticsCard progressCard = new StatisticsCard(
                    "In Progress", 
                    String.valueOf(myProgressTickets), 
                    VaadinIcon.COG, 
                    "contrast"
            );
            layout.add(progressCard);
            
            // My overdue tickets
            long myOverdueTickets = ticketService.countOverdueTicketsByAssignee(currentUser);
            StatisticsCard overdueCard = new StatisticsCard(
                    "My Overdue", 
                    String.valueOf(myOverdueTickets), 
                    VaadinIcon.CLOCK, 
                    "error"
            );
            layout.add(overdueCard);
        }
        
        return layout;
    }
    
    private HorizontalLayout createTenantStatisticsCards() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);
        
        User currentUser = securityService.getAuthenticatedUser().orElse(null);
        if (currentUser != null) {
            // My tickets
            long myTickets = ticketService.countTicketsByReporter(currentUser);
            StatisticsCard myCard = new StatisticsCard(
                    "My Tickets", 
                    String.valueOf(myTickets), 
                    VaadinIcon.FILE_TEXT, 
                    "primary"
            );
            layout.add(myCard);
            
            // My open tickets
            long myOpenTickets = ticketService.countTicketsByReporterAndStatus(currentUser, TicketStatus.OPEN);
            StatisticsCard openCard = new StatisticsCard(
                    "Open", 
                    String.valueOf(myOpenTickets), 
                    VaadinIcon.EXCLAMATION_CIRCLE, 
                    "error"
            );
            layout.add(openCard);
            
            // My resolved tickets
            long myResolvedTickets = ticketService.countTicketsByReporterAndStatus(currentUser, TicketStatus.RESOLVED);
            StatisticsCard resolvedCard = new StatisticsCard(
                    "Resolved", 
                    String.valueOf(myResolvedTickets), 
                    VaadinIcon.CHECK_CIRCLE, 
                    "success"
            );
            layout.add(resolvedCard);
            
            // My closed tickets
            long myClosedTickets = ticketService.countTicketsByReporterAndStatus(currentUser, TicketStatus.CLOSED);
            StatisticsCard closedCard = new StatisticsCard(
                    "Closed", 
                    String.valueOf(myClosedTickets), 
                    VaadinIcon.ARCHIVE, 
                    "contrast"
            );
            layout.add(closedCard);
        }
        
        return layout;
    }
    
    private HorizontalLayout createChartsLayout() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);
        
        // Tickets by status chart
        TicketChart statusChart = new TicketChart("Tickets by Status", TicketChart.ChartType.PIE);
        statusChart.setData(ticketService.getTicketCountByStatus());
        layout.add(statusChart);
        
        // Tickets by priority chart
        TicketChart priorityChart = new TicketChart("Tickets by Priority", TicketChart.ChartType.BAR);
        priorityChart.setData(ticketService.getTicketCountByPriority());
        layout.add(priorityChart);
        
        return layout;
    }
    
    private Component createRecentTicketsGrid() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("50%");
        layout.addClassName("recent-tickets");
        
        H2 title = new H2("Recent Tickets");
        layout.add(title);
        
        Grid<Ticket> grid = new Grid<>(Ticket.class, false);
        grid.addClassNames(LumoUtility.Border.NONE, LumoUtility.Padding.NONE);
        
        grid.addColumn(Ticket::getTitle).setHeader("Title").setAutoWidth(true);
        grid.addColumn(ticket -> ticket.getStatus().name()).setHeader("Status");
        grid.addColumn(ticket -> ticket.getPriority().name()).setHeader("Priority");
        
        // Add status indicator
        grid.addColumn(new ComponentRenderer<>(ticket -> {
            Icon icon = createStatusIcon(ticket.getStatus());
            Span span = new Span(icon, new Span(ticket.getStatus().name()));
            span.getElement().getThemeList().add("badge");
            return span;
        })).setHeader("Status");
        
        // Load recent tickets
        List<Ticket> recentTickets = ticketService.findRecentTickets(10);
        grid.setItems(recentTickets);
        
        layout.add(grid);
        return layout;
    }
    
    private Component createOverdueTicketsGrid() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("50%");
        layout.addClassName("overdue-tickets");
        
        H2 title = new H2("Overdue Tickets");
        layout.add(title);
        
        Grid<Ticket> grid = new Grid<>(Ticket.class, false);
        grid.addClassNames(LumoUtility.Border.NONE, LumoUtility.Padding.NONE);
        
        grid.addColumn(Ticket::getTitle).setHeader("Title").setAutoWidth(true);
        grid.addColumn(ticket -> ticket.getAssignee() != null ? 
                ticket.getAssignee().getFullName() : "Unassigned").setHeader("Assignee");
        grid.addColumn(Ticket::getEstimatedCompletion).setHeader("Due Date");
        
        // Load overdue tickets
        List<Ticket> overdueTickets = ticketService.findOverdueTickets();
        grid.setItems(overdueTickets);
        
        layout.add(grid);
        return layout;
    }
    
    private Component createAssignedTicketsGrid() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.addClassName("assigned-tickets");
        
        H2 title = new H2("My Assigned Tickets");
        layout.add(title);
        
        Grid<Ticket> grid = new Grid<>(Ticket.class, false);
        grid.addClassNames(LumoUtility.Border.NONE, LumoUtility.Padding.NONE);
        
        grid.addColumn(Ticket::getTitle).setHeader("Title").setAutoWidth(true);
        grid.addColumn(ticket -> ticket.getCategory().getName()).setHeader("Category");
        grid.addColumn(Ticket::getPriority).setHeader("Priority");
        grid.addColumn(Ticket::getStatus).setHeader("Status");
        grid.addColumn(Ticket::getEstimatedCompletion).setHeader("Due Date");
        
        // Load assigned tickets for current user
        User currentUser = securityService.getAuthenticatedUser().orElse(null);
        if (currentUser != null) {
            List<Ticket> assignedTickets = ticketService.findTicketsByAssignee(currentUser);
            grid.setItems(assignedTickets);
        }
        
        layout.add(grid);
        return layout;
    }
    
    private Component createMyTicketsGrid() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.addClassName("my-tickets");
        
        H2 title = new H2("My Tickets");
        layout.add(title);
        
        Grid<Ticket> grid = new Grid<>(Ticket.class, false);
        grid.addClassNames(LumoUtility.Border.NONE, LumoUtility.Padding.NONE);
        
        grid.addColumn(Ticket::getTitle).setHeader("Title").setAutoWidth(true);
        grid.addColumn(ticket -> ticket.getCategory().getName()).setHeader("Category");
        grid.addColumn(Ticket::getPriority).setHeader("Priority");
        grid.addColumn(Ticket::getStatus).setHeader("Status");
        grid.addColumn(Ticket::getCreatedAt).setHeader("Created");
        
        // Load tickets for current user
        User currentUser = securityService.getAuthenticatedUser().orElse(null);
        if (currentUser != null) {
            List<Ticket> myTickets = ticketService.findTicketsByReporter(currentUser);
            grid.setItems(myTickets);
        }
        
        layout.add(grid);
        return layout;
    }
    
    private Component createQuickActionsPanel() {
        VerticalLayout layout = new VerticalLayout();
        layout.setWidthFull();
        layout.addClassName("quick-actions");
        
        H2 title = new H2("Quick Actions");
        layout.add(title);
        
        HorizontalLayout actionsLayout = new HorizontalLayout();
        actionsLayout.setSpacing(true);
        
        Button createTicketBtn = new Button("Create New Ticket", e -> {
            // Navigate to create ticket view
            getUI().ifPresent(ui -> ui.navigate("tickets/new"));
        });
        createTicketBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        actionsLayout.add(createTicketBtn);
        
        Button viewAllBtn = new Button("View All Tickets", e -> {
            // Navigate to tickets view
            getUI().ifPresent(ui -> ui.navigate("tickets"));
        });
        actionsLayout.add(viewAllBtn);
        
        layout.add(actionsLayout);
        return layout;
    }
    
    private Icon createStatusIcon(TicketStatus status) {
        Icon icon;
        switch (status) {
            case OPEN:
                icon = VaadinIcon.EXCLAMATION_CIRCLE.create();
                icon.getElement().getThemeList().add("error");
                break;
            case IN_PROGRESS:
                icon = VaadinIcon.COG.create();
                icon.getElement().getThemeList().add("contrast");
                break;
            case ON_HOLD:
                icon = VaadinIcon.PAUSE_CIRCLE.create();
                icon.getElement().getThemeList().add("contrast");
                break;
            case RESOLVED:
                icon = VaadinIcon.CHECK_CIRCLE.create();
                icon.getElement().getThemeList().add("success");
                break;
            case CLOSED:
                icon = VaadinIcon.ARCHIVE.create();
                icon.getElement().getThemeList().add("success");
                break;
            default:
                icon = VaadinIcon.QUESTION_CIRCLE.create();
                break;
        }
        return icon;
    }
}