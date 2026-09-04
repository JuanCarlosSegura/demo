package com.example.meteo.demo.scheduler;

import com.example.meteo.demo.service.ApiClientService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DataPollerScheduler {

    private final ApiClientService apiService;
    private volatile Map<String, String> latestWeather = Map.of();
    private volatile Map<String, String> latestAirQuality = Map.of();

    public DataPollerScheduler(ApiClientService apiService) {
        this.apiService = apiService;
    }

    // La petición es asíncrona para que una API lenta no bloquee el scheduler.
    @Scheduled(fixedRate = 15000)
    public void pollAndPublish() {
        System.out.println("[SCHEDULER] Recuperando actualización de Open-Meteo...");
        apiService.fetchWeather()
                .thenAccept(data -> latestWeather = data)
                .exceptionally(error -> logError("meteorología", error));
        apiService.fetchAirQuality()
                .thenAccept(data -> latestAirQuality = data)
                .exceptionally(error -> logError("calidad del aire", error));
    }

    private Void logError(String source, Throwable error) {
        System.err.println("[ERROR] No se pudo consultar " + source + ": " + error.getMessage());
        return null;
    }

    public Map<String, String> getLatestWeather() {
        return latestWeather;
    }

    public Map<String, String> getLatestAirQuality() {
        return latestAirQuality;
    }
}
