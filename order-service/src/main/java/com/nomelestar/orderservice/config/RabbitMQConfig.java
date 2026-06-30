package com.nomelestar.orderservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para order-service.
 * Define la infraestructura de mensajería (Exchanges, Queues, Bindings) y los serializadores de datos
 * para la comunicación asíncrona entre microservicios utilizando el protocolo AMQP.
 */
@Configuration
public class RabbitMQConfig {

    // Nombre del Exchange tipo Topic compartido para mensajería de productos.
    public static final String EXCHANGE_NAME = "product.exchange";
    
    // Cola encargada de recibir eventos de creación de productos.
    public static final String QUEUE_NAME = "order.product.created";
    
    // Llave de enrutamiento (routing key) para suscribirse a eventos de productos creados.
    public static final String ROUTING_KEY = "product.created";
    
    // Llave de enrutamiento para publicar eventos de órdenes creadas.
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
     * Declara la cola local para recibir los eventos de creación de productos.
     */
    @Bean
    public Queue queue() {
        return new Queue(QUEUE_NAME);
    }

    /**
     * Define la relación (Binding) que asocia la cola con el Exchange usando la clave de enrutamiento.
     */
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
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
