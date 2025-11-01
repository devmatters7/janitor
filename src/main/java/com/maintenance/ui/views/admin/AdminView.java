package com.maintenance.ui.views.admin;

import com.maintenance.entity.User;
import com.maintenance.entity.enums.Role;
import com.maintenance.security.SecurityService;
import com.maintenance.service.UserService;
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
import jakarta.annotation.security.RolesAllowed;

import java.util.List;

@Route(value = "admin", layout = com.maintenance.ui.MainLayout.class)
@PageTitle("Admin | Building Maintenance System")
@RolesAllowed("ADMIN")
public class AdminView extends VerticalLayout {
    
    private final UserService userService;
    private final SecurityService securityService;
    
    private final Grid<User> userGrid;
    private final Button createUserButton;
    private final Button refreshButton;
    
    public AdminView(UserService userService, SecurityService securityService) {
        this.userService = userService;
        this.securityService = securityService;
        
        addClassName("admin-view");
        setSpacing(true);
        setPadding(true);
        setSizeFull();
        
        // Add title
        H2 title = new H2("System Administration");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM);
        add(title);
        
        // Create action buttons
        createActionButtons();
        
        // Create user grid
        userGrid = createUserGrid();
        
        // Add components to layout
        add(createUserButton, refreshButton, userGrid);
        
        // Load initial data
        loadUsers();
    }
    
    private void createActionButtons() {
        // Create user button
        createUserButton = new Button("Create User", VaadinIcon.PLUS.create());
        createUserButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createUserButton.addClickListener(event -> createNewUser());
        
        // Refresh button
        refreshButton = new Button("Refresh", VaadinIcon.REFRESH.create());
        refreshButton.addClickListener(event -> loadUsers());
    }
    
    private Grid<User> createUserGrid() {
        Grid<User> grid = new Grid<>(User.class, false);
        grid.setWidthFull();
        grid.setHeight("600px");
        
        // Configure columns
        configureGridColumns(grid);
        
        // Add click listener for row selection
        grid.addItemClickListener(event -> {
            User user = event.getItem();
            openUserDetails(user);
        });
        
        return grid;
    }
    
    private void configureGridColumns(Grid<User> grid) {
        grid.addColumn(User::getUsername)
                .setHeader("Username")
                .setAutoWidth(true)
                .setFlexGrow(1);
        
        grid.addColumn(User::getEmail)
                .setHeader("Email")
                .setAutoWidth(true)
                .setFlexGrow(1);
        
        grid.addColumn(user -> user.getFirstName() + " " + user.getLastName())
                .setHeader("Full Name")
                .setAutoWidth(true)
                .setFlexGrow(1);
        
        grid.addColumn(User::getRole)
                .setHeader("Role")
                .setAutoWidth(true);
        
        grid.addColumn(User::getPhoneNumber)
                .setHeader("Phone")
                .setAutoWidth(true);
        
        grid.addColumn(user -> user.getIsActive() ? "Active" : "Inactive")
                .setHeader("Status")
                .setAutoWidth(true);
        
        grid.addColumn(User::getCreatedAt)
                .setHeader("Created")
                .setAutoWidth(true);
        
        // Action column for edit/deactivate
        grid.addComponentColumn(user -> {
            HorizontalLayout actions = new HorizontalLayout();
            
            Button editButton = new Button("Edit", VaadinIcon.EDIT.create());
            editButton.addClickListener(event -> editUser(user));
            
            Button deactivateButton = new Button(
                    user.getIsActive() ? "Deactivate" : "Activate",
                    user.getIsActive() ? VaadinIcon.CLOSE.create() : VaadinIcon.CHECK.create()
            );
            deactivateButton.addClickListener(event -> toggleUserStatus(user));
            
            actions.add(editButton, deactivateButton);
            return actions;
        }).setHeader("Actions").setAutoWidth(true);
    }
    
    private void loadUsers() {
        List<User> users = userService.findAllUsers();
        userGrid.setItems(users);
    }
    
    private void createNewUser() {
        // Navigate to user creation view
        getUI().ifPresent(ui -> ui.navigate("admin/users/new"));
    }
    
    private void editUser(User user) {
        // Navigate to user edit view
        getUI().ifPresent(ui -> ui.navigate("admin/users/" + user.getId()));
    }
    
    private void toggleUserStatus(User user) {
        if (user.getIsActive()) {
            userService.deactivateUser(user.getId());
        } else {
            userService.activateUser(user.getId());
        }
        loadUsers();
    }
    
    private void openUserDetails(User user) {
        // Navigate to user details view
        getUI().ifPresent(ui -> ui.navigate("admin/users/" + user.getId()));
    }
}