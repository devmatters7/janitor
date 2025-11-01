package com.maintenance.ui;

import com.maintenance.entity.User;
import com.maintenance.entity.enums.Role;
import com.maintenance.security.SecurityService;
import com.maintenance.ui.views.admin.AdminView;
import com.maintenance.ui.views.dashboard.DashboardView;
import com.maintenance.ui.views.reports.ReportsView;
import com.maintenance.ui.views.tickets.TicketView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@CssImport(value = "./styles/main-layout.css")
public class MainLayout extends AppLayout {
    
    private final SecurityService securityService;
    private final Tabs menu;
    private H1 viewTitle;
    
    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;
        
        // Create the menu
        menu = createMenuTabs();
        
        // Add the menu to the drawer
        addToDrawer(createDrawerContent(menu));
        
        // Add the header to the navbar
        addToNavbar(createHeaderContent());
        
        // Configure the drawer
        setPrimarySection(Section.DRAWER);
        
        // Set the content padding
        setContentPadding(true);
    }
    
    private Component createHeaderContent() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setId("header");
        layout.getThemeList().add("dark");
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.addClassNames(LumoUtility.Padding.Vertical.NONE, LumoUtility.Padding.Horizontal.MEDIUM);
        
        // Drawer toggle
        layout.add(new DrawerToggle());
        
        // View title
        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        layout.add(viewTitle);
        
        layout.add(new H2("Building Maintenance System"));
        
        // Spacer to push user menu to the right
        layout.addAndExpand(new Span());
        
        // User menu
        if (securityService.getAuthenticatedUser().isPresent()) {
            User user = securityService.getAuthenticatedUser().get();
            layout.add(createUserMenu(user));
        }
        
        return layout;
    }
    
    private Component createUserMenu(User user) {
        Avatar avatar = new Avatar(user.getFullName());
        avatar.addClassNames(LumoUtility.Margin.Right.SMALL);
        
        // Create a menu bar for the user
        MenuBar userMenu = new MenuBar();
        userMenu.setThemeName("tertiary-inline contrast");
        
        // Add the avatar and username
        String userName = user.getFullName() + " (" + user.getRole().name() + ")";
        userMenu.addItem(userName, e -> {}).getElement().appendChild(avatar.getElement());
        
        // Add menu items
        userMenu.addItem("Profile", e -> {
            // Navigate to profile view
            getUI().ifPresent(ui -> ui.navigate("profile"));
        });
        
        userMenu.addItem("Settings", e -> {
            // Navigate to settings view
            getUI().ifPresent(ui -> ui.navigate("settings"));
        });
        
        userMenu.addItem("Sign Out", e -> logout());
        
        return userMenu;
    }
    
    private Component createDrawerContent(Tabs menu) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getThemeList().set("spacing-s", true);
        layout.setAlignItems(FlexComponent.Alignment.STRETCH);
        
        // Header with logo/app name
        HorizontalLayout header = new HorizontalLayout();
        header.setId("logo");
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.add(new Icon(VaadinIcon.HOME));
        header.add(new H1("Maintenance System"));
        
        // Navigation
        layout.add(header, menu);
        
        return layout;
    }
    
    private Tabs createMenuTabs() {
        Tabs tabs = new Tabs();
        tabs.setOrientation(Tabs.Orientation.VERTICAL);
        tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
        tabs.setId("tabs");
        
        // Add menu tabs based on user role
        tabs.add(createTab(VaadinIcon.DASHBOARD, "Dashboard", DashboardView.class));
        tabs.add(createTab(VaadinIcon.TICKET, "Tickets", TicketView.class));
        tabs.add(createTab(VaadinIcon.CHART, "Reports", ReportsView.class));
        
        // Admin-only tabs
        if (securityService.getAuthenticatedUser().isPresent()) {
            User user = securityService.getAuthenticatedUser().get();
            if (user.getRole() == Role.ADMIN) {
                tabs.add(createTab(VaadinIcon.COG, "Admin", AdminView.class));
            }
        }
        
        // Set the first tab as selected
        tabs.setSelectedIndex(0);
        
        return tabs;
    }
    
    private Tab createTab(VaadinIcon viewIcon, String viewName, Class<? extends Component> viewClass) {
        Icon icon = viewIcon.create();
        icon.getStyle().set("box-sizing", "border-box")
                .set("margin-inline-end", "var(--lumo-space-m)")
                .set("margin-inline-start", "var(--lumo-space-xs)")
                .set("padding", "var(--lumo-space-xs)");
        
        RouterLink link = new RouterLink();
        link.add(icon, new Span(viewName));
        link.setRoute(viewClass);
        link.setTabIndex(-1);
        
        return new Tab(link);
    }
    
    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        
        // Update the view title
        String title = getContent().getClass().getSimpleName()
                .replace("View", "");
        viewTitle.setText(title);
        
        // Highlight the selected tab
        String target = getContent().getClass().getSimpleName();
        menu.getChildren()
                .filter(tab -> tab instanceof Tab)
                .map(tab -> (Tab) tab)
                .filter(tab -> tab.getComponentAt(0) instanceof RouterLink)
                .map(tab -> (RouterLink) tab.getComponentAt(0))
                .filter(routerLink -> routerLink.getTarget().getSimpleName().equals(target))
                .findFirst()
                .ifPresent(routerLink -> {
                    Tab selectedTab = (Tab) routerLink.getParent().get();
                    menu.setSelectedTab(selectedTab);
                });
    }
    
    private void logout() {
        // Perform logout
        new SecurityContextLogoutHandler().logout(
                VaadinServletRequest.getCurrent().getHttpServletRequest(), 
                null, 
                null);
        
        // Redirect to login page
        getUI().ifPresent(ui -> ui.getPage().setLocation("/login"));
    }
}