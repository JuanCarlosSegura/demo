package com.example.meteo.demo.controller;

import com.example.meteo.demo.scheduler.DataPollerScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class WeatherViewController {

    private final DataPollerScheduler scheduler;

    public WeatherViewController(DataPollerScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("initialWeather", scheduler.getLatestWeather());
        model.addAttribute("initialAirQuality", scheduler.getLatestAirQuality());
        return "index";
    }

    @GetMapping("/api/weather/latest")
    @ResponseBody
    public Map<String, Object> getLatestWeather() {
        return Map.of(
                "weather", scheduler.getLatestWeather(),
                "airQuality", scheduler.getLatestAirQuality());
    }
}
