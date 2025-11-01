package com.maintenance.ui.views.tickets;

import com.maintenance.entity.Ticket;
import com.maintenance.entity.enums.TicketStatus;
import com.maintenance.security.SecurityService;
import com.maintenance.service.TicketService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "tickets", layout = com.maintenance.ui.MainLayout.class)
@PageTitle("Tickets | Building Maintenance System")
@PermitAll
public class TicketView extends VerticalLayout {
    
    private final TicketService ticketService;
    private final SecurityService securityService;
    
    private final Grid<Ticket> ticketGrid;
    private final Button createButton;
    private final Button refreshButton;
    
    public TicketView(TicketService ticketService, SecurityService securityService) {
        this.ticketService = ticketService;
        this.securityService = securityService;
        
        addClassName("tickets-view");
        setSpacing(true);
        setPadding(true);
        setSizeFull();
        
        // Add title
        H2 title = new H2("Maintenance Tickets");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        add(title);
        
        // Create action buttons
        createActionButtons();
        
        // Create ticket grid
        ticketGrid = createTicketGrid();
        
        // Add components to layout
        add(createButton, refreshButton, ticketGrid);
        
        // Load initial data
        loadTickets();
    }
    
    private void createActionButtons() {
        // Create ticket button
        createButton = new Button("Create Ticket", VaadinIcon.PLUS.create());
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClickListener(event -> createNewTicket());
        
        // Refresh button
        refreshButton = new Button("Refresh", VaadinIcon.REFRESH.create());
        refreshButton.addClickListener(event -> loadTickets());
    }
    
    private Grid<Ticket> createTicketGrid() {
        Grid<Ticket> grid = new Grid<>(Ticket.class, false);
        grid.setWidthFull();
        grid.setHeight("600px");
        
        // Configure columns based on user role
        configureGridColumns(grid);
        
        // Add click listener for row selection
        grid.addItemClickListener(event -> {
            Ticket ticket = event.getItem();
            openTicketDetails(ticket);
        });
        
        return grid;
    }
    
    private void configureGridColumns(Grid<Ticket> grid) {
        // Common columns for all users
        grid.addColumn(Ticket::getTitle)
                .setHeader("Title")
                .setAutoWidth(true)
                .setFlexGrow(1);
        
        grid.addColumn(ticket -> ticket.getStatus().name())
                .setHeader("Status")
                .setAutoWidth(true);
        
        grid.addColumn(ticket -> ticket.getPriority().name())
                .setHeader("Priority")
                .setAutoWidth(true);
        
        grid.addColumn(ticket -> ticket.getCategory().getName())
                .setHeader("Category")
                .setAutoWidth(true);
        
        grid.addColumn(ticket -> ticket.getReporter().getFullName())
                .setHeader("Reporter")
                .setAutoWidth(true);
        
        grid.addColumn(ticket -> ticket.getBuilding().getName())
                .setHeader("Building")
                .setAutoWidth(true);
        
        grid.addColumn(Ticket::getCreatedAt)
                .setHeader("Created")
                .setAutoWidth(true);
        
        // Additional columns for admins and technicians
        if (securityService.isAdmin() || securityService.isTechnician()) {
            grid.addColumn(ticket -> {
                        if (ticket.getAssignee() != null) {
                            return ticket.getAssignee().getFullName();
                        }
                        return "Unassigned";
                    })
                    .setHeader("Assignee")
                    .setAutoWidth(true);
            
            grid.addColumn(Ticket::getEstimatedCompletion)
                    .setHeader("Due Date")
                    .setAutoWidth(true);
        }
    }
    
    private void loadTickets() {
        List<Ticket> tickets;
        
        // Load tickets based on user role
        if (securityService.isAdmin() || securityService.isTechnician()) {
            tickets = ticketService.findAllTickets();
        } else {
            // For tenants, show only their tickets
            securityService.getAuthenticatedUser().ifPresent(user -> {
                List<Ticket> userTickets = ticketService.findTicketsByReporter(user);
                ticketGrid.setItems(userTickets);
            });
            return;
        }
        
        ticketGrid.setItems(tickets);
    }
    
    private void createNewTicket() {
        // Navigate to ticket creation view
        getUI().ifPresent(ui -> ui.navigate("tickets/new"));
    }
    
    private void openTicketDetails(Ticket ticket) {
        // Navigate to ticket details view
        getUI().ifPresent(ui -> ui.navigate("tickets/" + ticket.getId()));
    }
}