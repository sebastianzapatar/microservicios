package com.nomelestar.orderservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.kafka.support.converter.StringJacksonJsonMessageConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase de configuración para Apache Kafka en el servicio de órdenes (order-service).
 * Esta clase define la infraestructura necesaria para producir y consumir mensajes,
 * incluyendo las fábricas de productores y consumidores, las plantillas de Kafka,
 * los convertidores de mensajes JSON y la creación automática de tópicos.
 *
 * Se utiliza la anotación @EnableKafka para habilitar la detección de oyentes Kafka (@KafkaListener).
 */
@EnableKafka
@Configuration
public class KafkaConfig {

    // Nombre del tópico de Kafka para notificar la creación de órdenes.
    public static final String ORDER_CREATED_TOPIC_NAME = "order.created";

    // Dirección de los servidores de Kafka. Por defecto se usa localhost:29092 para desarrollo local.
    // En Docker se inyecta la variable de entorno SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092.
    @Value("${spring.kafka.bootstrap-servers:localhost:29092}")
    private String bootstrapServers;

    /**
     * Define la fábrica de productores (ProducerFactory).
     * Se encarga de instanciar y configurar los productores de Kafka.
     * Configura la clave como String y el valor como JSON (JacksonJsonSerializer).
     *
     * @return Una instancia de ProducerFactory configurada.
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // JacksonJsonSerializer es la clase recomendada en Spring Kafka 4.x para reemplazar la serialización Json clásica.
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Define la plantilla de Kafka (KafkaTemplate) para enviar mensajes de forma síncrona o asíncrona.
     * Envía objetos utilizando el ProducerFactory configurado previamente.
     *
     * @return Una instancia de KafkaTemplate.
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Define la fábrica de consumidores (ConsumerFactory).
     * Configura cómo se conectarán los consumidores al clúster de Kafka y cómo leerán los mensajes.
     * Mapea la clave y el valor como String puro para que el convertidor posterior convierta a objeto de negocio.
     *
     * @return Una instancia de ConsumerFactory.
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Lee desde el inicio si es un grupo nuevo

        // Inicializamos la fábrica usando StringDeserializer tanto para la clave como para el valor
        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new StringDeserializer());
    }

    /**
     * Configura el contenedor que gestionará los hilos de los oyentes (@KafkaListener).
     * Se inyecta el convertidor de mensajes StringJacksonJsonMessageConverter de Spring Kafka 4.x,
     * permitiendo que los payloads JSON en crudo recibidos como String se transformen automáticamente
     * en los DTOs que especifiquemos en los parámetros de los métodos oyentes.
     *
     * @return Una instancia de ConcurrentKafkaListenerContainerFactory configurada.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        // StringJacksonJsonMessageConverter deserializa el JSON crudo en objetos Java basándose en el tipo del parámetro del listener.
        factory.setRecordMessageConverter(new StringJacksonJsonMessageConverter());
        return factory;
    }

    /**
     * Bean para la creación automática del tópico de creación de órdenes.
     * Si el tópico "order.created" no existe al arrancar la aplicación, KafkaAdmin lo creará con estas propiedades.
     *
     * @return Una instancia de NewTopic con 1 partición y 1 réplica.
     */
    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name(ORDER_CREATED_TOPIC_NAME)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
