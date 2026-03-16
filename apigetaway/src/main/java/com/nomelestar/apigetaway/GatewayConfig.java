package com.nomelestar.apigetaway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class GatewayConfig {

    @Value("${PRODUCT_SERVICE_URL:http://product-service:8084}")
    private String productServiceUrl;

    @Value("${ORDER_SERVICE_URL:http://order-service:8083}")
    private String orderServiceUrl;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("product-service-java", r -> r.path("/api/products/**")
                        .uri(productServiceUrl))
                .route("order-service-java", r -> r.path("/api/orders/**")
                        .uri(orderServiceUrl))
                .build();
    }
}
