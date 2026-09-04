package com.example.meteo.demo.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate, 
                                @Value("${app.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void sendMessage(String message) {
        kafkaTemplate.send(topic, message);
        System.out.println("[KAFKA] --> Mensaje enviado correctamente al tópico: " + topic);
    }
}
