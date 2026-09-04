package com.example.meteo.demo.scheduler;

import com.example.meteo.demo.service.ApiClientService;
import com.example.meteo.demo.kafka.KafkaProducerService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DataPollerScheduler {

    private final ApiClientService apiService;
    private final KafkaProducerService kafkaProducer;
    private String latestJsonCache = "{\"status\": \"Esperando primera sincronización...\"}";

    public DataPollerScheduler(ApiClientService apiService, KafkaProducerService kafkaProducer) {
        this.apiService = apiService;
        this.kafkaProducer = kafkaProducer;
    }

    // El programador descarga de la API y publica en Kafka cada 15 segundos
    @Scheduled(fixedRate = 15000)
    public void pollAndPublish() {
        System.out.println("[SCHEDULER] Recuperando actualización de Open-Meteo...");
        String weatherData = apiService.fetchLatestData();
        
        if (weatherData != null && !weatherData.contains("error")) {
            this.latestJsonCache = weatherData;
            kafkaProducer.sendMessage(weatherData);
        } else {
            System.err.println("[ERROR] No se pudo enviar el mensaje a Kafka.");
        }
    }

    public String getLatestJsonCache() {
        return this.latestJsonCache;
    }
}
