package com.example.meteo.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class ApiClientService {

    private final RestClient restClient;

    public ApiClientService(@Value("${app.api.url}") String apiUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
    }

    public String fetchLatestData() {
        try {
            return restClient.get()
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            return "{\"error\": \"Error al conectar con la API de Clima: " + e.getMessage() + "\"}";
        }
    }
}
