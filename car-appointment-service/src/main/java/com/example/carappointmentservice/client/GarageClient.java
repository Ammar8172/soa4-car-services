package com.example.carappointmentservice.client;

import com.example.carappointmentservice.dto.Garage;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class GarageClient {

    private final WebClient webClient;

    // Injects the pre-configured WebClient for calling the garage service
    public GarageClient(WebClient garageWebClient) {
        this.webClient = garageWebClient;
    }

    // Fetches the full list of garages from the garage service
    public Mono<List<Garage>> getAllGarages() {
        return webClient.get()
                .uri("/garages")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new IllegalArgumentException("Unable to load garage list")))
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new IllegalStateException("Garage service error")))
                .bodyToFlux(Garage.class)
                .collectList();
    }

    // Fetches a single garage by its ID from the garage service
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

    // Returns true if a garage with the given ID exists, false otherwise
    public Mono<Boolean> garageExists(Long garageId) {
        return getGarageById(garageId)
                .map(garage -> true)
                .onErrorReturn(false);
    }
}
