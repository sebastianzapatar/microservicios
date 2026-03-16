package com.nomelestar.apigetaway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class RouteDebugger {
    private static final Logger log = LoggerFactory.getLogger(RouteDebugger.class);
    private final RouteLocator routeLocator;

    public RouteDebugger(RouteLocator routeLocator) {
        this.routeLocator = routeLocator;
    }

    @PostConstruct
    public void dumpRoutes() {
        log.info("============== DUMPING ROUTES ==============");
        routeLocator.getRoutes().subscribe(route -> {
            log.info("ROUTE ID: {}, URI: {}", route.getId(), route.getUri());
        });
    }
}
