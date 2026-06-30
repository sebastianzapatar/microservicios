package com.nomelestar.productservice.config;

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
 * Clase de configuración para Apache Kafka en el servicio de productos (product-service).
 * Define la infraestructura para producir y consumir mensajes, y registrar tópicos.
 *
 * Utiliza @EnableKafka para activar el procesamiento de métodos anotados con @KafkaListener.
 */
@EnableKafka
@Configuration
public class KafkaConfig {

    // Nombres de los tópicos para eventos de productos.
    public static final String TOPIC_NAME = "product.created";
    public static final String UPDATED_TOPIC_NAME = "product.updated";

    // Dirección de los servidores de Kafka. Por defecto localhost:29092.
    // En Docker es reemplazado por kafka:9092 a través de variables de entorno.
    @Value("${spring.kafka.bootstrap-servers:localhost:29092}")
    private String bootstrapServers;

    /**
     * Define la fábrica de productores (ProducerFactory).
     * Configura la serialización de la clave como String y del valor como JSON
     * utilizando JacksonJsonSerializer para compatibilidad con Jackson 3 en Spring Kafka 4.x.
     *
     * @return Fábrica de productores configurada.
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Define el KafkaTemplate que permite a los servicios enviar mensajes de forma programática.
     *
     * @return Instancia de KafkaTemplate.
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Fábrica de consumidores (ConsumerFactory).
     * Configura los consumidores de product-service. 
     * Configura la clave y el valor como StringDeserializer para delegar la conversión JSON a Jackson.
     *
     * @return Fábrica de consumidores.
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "product-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new StringDeserializer());
    }

    /**
     * Configura el contenedor para oyentes (@KafkaListener).
     * Utiliza StringJacksonJsonMessageConverter (nuevo en Spring Kafka 4.x) para deserializar automáticamente
     * los mensajes JSON recibidos como texto plano a clases DTO específicas en el oyente.
     *
     * @return Fábrica de contenedores de oyentes Kafka.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setRecordMessageConverter(new StringJacksonJsonMessageConverter());
        return factory;
    }

    /**
     * Bean para la creación del tópico de productos creados.
     *
     * @return Bean del nuevo tópico "product.created".
     */
    @Bean
    public NewTopic productCreatedTopic() {
        return TopicBuilder.name(TOPIC_NAME)
                .partitions(1)
                .replicas(1)
                .build();
    }

    /**
     * Bean para la creación del tópico de productos actualizados.
     *
     * @return Bean del nuevo tópico "product.updated".
     */
    @Bean
    public NewTopic productUpdatedTopic() {
        return TopicBuilder.name(UPDATED_TOPIC_NAME)
                .partitions(1)
                .replicas(1)
                .build();
    }
}

