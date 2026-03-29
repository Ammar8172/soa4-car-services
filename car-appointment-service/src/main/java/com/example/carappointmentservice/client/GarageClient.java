package com.example.carappointmentservice.client;

import com.example.carappointmentservice.dto.Garage;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class GarageClient {

    private final WebClient webClient;

    public GarageClient(WebClient garageWebClient) {
        this.webClient = garageWebClient;
    }

    public Mono<Garage> getGarageById(Long garageId) {
        return webClient.get()
                .uri("/garages/{id}", garageId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new IllegalArgumentException("Garage not found for id " + garageId)))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new IllegalStateException("Garage service error")))
                .bodyToMono(Garage.class);
    }

    public Mono<Boolean> garageExists(Long garageId) {
        return getGarageById(garageId)
                .map(garage -> true)
                .onErrorReturn(false);
    }
}
