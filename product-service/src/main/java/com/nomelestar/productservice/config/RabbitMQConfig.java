package com.nomelestar.productservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para product-service.
 * Define la infraestructura de mensajería (Exchanges, Queues, Bindings) y los serializadores de datos
 * para posibilitar la sincronización asíncrona de inventarios y datos del catálogo.
 */
@Configuration
public class RabbitMQConfig {

    // Nombre del Exchange tipo Topic compartido para mensajería de productos.
    public static final String EXCHANGE_NAME = "product.exchange";
    
    // Llave de enrutamiento (routing key) para publicar eventos de creación de productos.
    public static final String ROUTING_KEY = "product.created";

    // Nombre de la cola encargada de recibir eventos de creación de órdenes.
    public static final String ORDER_QUEUE_NAME = "product.order.created";
    
    // Llave de enrutamiento para suscribirse a eventos de creación de órdenes.
    public static final String ORDER_ROUTING_KEY = "order.created";

    /**
     * Declara un Topic Exchange. Este tipo de Exchange permite enrutar mensajes
     * a diferentes colas basándose en patrones de routing key.
     */
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    /**
     * Declara la cola local para recibir los eventos de creación de órdenes.
     */
    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE_NAME);
    }

    /**
     * Define la relación (Binding) que asocia la cola con el Exchange usando la clave de enrutamiento.
     */
    @Bean
    public Binding orderBinding(Queue orderQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderQueue).to(exchange).with(ORDER_ROUTING_KEY);
    }

    /**
     * Configura el convertidor de mensajes de Spring AMQP para utilizar Jackson.
     * Esto permite serializar y deserializar automáticamente los objetos de eventos en formato JSON.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
