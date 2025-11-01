package com.maintenance.ui.components;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class StatisticsCard extends Div {
    
    private final H3 value;
    private final Span title;
    private final Icon icon;
    
    public StatisticsCard(String titleText, String valueText, Icon icon, String theme) {
        addClassNames(
                LumoUtility.Background.BASE,
                LumoUtility.BorderRadius.LARGE,
                LumoUtility.BoxShadow.SMALL,
                LumoUtility.Padding.MEDIUM,
                LumoUtility.Display.FLEX,
                LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Height.UNDEFINED
        );
        
        // Create icon
        this.icon = icon;
        this.icon.addClassNames(
                LumoUtility.IconSize.LARGE,
                LumoUtility.TextColor.PRIMARY
        );
        
        // Apply theme
        if (theme != null && !theme.isEmpty()) {
            this.icon.getElement().getThemeList().add(theme);
        }
        
        // Create title
        this.title = new Span(titleText);
        this.title.addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.SECONDARY,
                LumoUtility.FontWeight.MEDIUM
        );
        
        // Create value
        this.value = new H3(valueText);
        this.value.addClassNames(
                LumoUtility.Margin.NONE,
                LumoUtility.FontSize.XXXLARGE,
                LumoUtility.FontWeight.EXTRABOLD
        );
        
        // Create layout
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.add(this.title, this.icon);
        
        VerticalLayout contentLayout = new VerticalLayout();
        contentLayout.setPadding(false);
        contentLayout.setSpacing(false);
        contentLayout.add(headerLayout, this.value);
        
        add(contentLayout);
    }
    
    public void setValue(String valueText) {
        this.value.setText(valueText);
    }
    
    public void setTitle(String titleText) {
        this.title.setText(titleText);
    }
    
    public void setIcon(Icon newIcon) {
        this.icon.getElement().removeFromParent();
        this.icon = newIcon;
        // Re-add the icon to the layout if needed
    }
}