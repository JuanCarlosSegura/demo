package com.example.meteo.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.Map;

@Service
public class ApiClientService {

    private final String weatherUrl;
    private final String airQualityUrl;
    private final HttpClient httpClient;

    public ApiClientService(@Value("${app.api.weather-url}") String weatherUrl,
                            @Value("${app.api.air-quality-url}") String airQualityUrl) {
        this.weatherUrl = weatherUrl;
        this.airQualityUrl = airQualityUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public CompletableFuture<Map<String, String>> fetchWeather() {
        return fetchForCities(weatherUrl, false);
    }

    public CompletableFuture<Map<String, String>> fetchAirQuality() {
        return fetchForCities(airQualityUrl, true);
    }

    private CompletableFuture<Map<String, String>> fetchForCities(String baseUrl, boolean airQuality) {
        Map<String, String> cities = Map.of(
                "Sevilla", "37.375,-6.0",
                "Isla Cristina (Huelva)", "37.2,-7.32",
                "Santa Fe (Granada)", "37.19,-3.72");
        CompletableFuture<?>[] requests = cities.entrySet().stream()
                .map(entry -> fetch(buildUrl(baseUrl, entry.getValue(), airQuality))
                        .thenApply(data -> Map.entry(entry.getKey(), data)))
                .toArray(CompletableFuture<?>[]::new);
        return CompletableFuture.allOf(requests)
                .thenApply(ignored -> {
                    Map<String, String> result = new java.util.HashMap<>();
                    for (CompletableFuture<?> request : requests) {
                        Map.Entry<?, ?> entry = (Map.Entry<?, ?>) request.join();
                        result.put((String) entry.getKey(), (String) entry.getValue());
                    }
                    return result;
                });
    }

    private String buildUrl(String baseUrl, String coordinates, boolean airQuality) {
        String[] values = coordinates.split(",");
        String variables = airQuality ? "pm10,pm2_5" : "temperature_2m,precipitation";
        return baseUrl + "?latitude=" + values[0] + "&longitude=" + values[1]
                + "&hourly=" + variables + "&timezone=Europe/Madrid&forecast_days=2";
    }

    private CompletableFuture<String> fetch(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header(HttpHeaders.USER_AGENT, "meteo-demo/1.0")
                .header(HttpHeaders.ACCEPT, "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        return CompletableFuture.completedFuture(response.body());
                    }
                    return CompletableFuture.failedFuture(
                            new IllegalStateException("HTTP " + response.statusCode() + ": " + response.body()));
                });
    }
}
