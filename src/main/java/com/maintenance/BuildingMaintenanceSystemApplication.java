package com.maintenance;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@ConfigurationPropertiesScan
@Theme(value = "building-maintenance")
@PWA(name = "Building Maintenance System", shortName = "BMS", offlineResources = {"images/logo.png"})
public class BuildingMaintenanceSystemApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(BuildingMaintenanceSystemApplication.class, args);
    }
}