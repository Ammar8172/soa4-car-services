package com.example.carappointmentservice.controller;

import com.example.carappointmentservice.dto.AppointmentResponse;
import com.example.carappointmentservice.entity.CarAppointment;
import com.example.carappointmentservice.service.CarAppointmentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/appointments")
public class CarAppointmentController {

    private final CarAppointmentService appointmentService;
    private final ObjectMapper objectMapper;

    public CarAppointmentController(CarAppointmentService appointmentService, ObjectMapper objectMapper) {
        this.appointmentService = appointmentService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public Mono<ResponseEntity<List<AppointmentResponse>>> getAllAppointments(
            @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {

        return appointmentService.getAllAppointmentsWithGarage()
                .map(appointments -> {
                    String etag = generateEtag(appointments);

                    if (etag.equals(normalizeEtag(ifNoneMatch))) {
                        return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                                .eTag(etag)
                                .build();
                    }

                    return ResponseEntity.ok()
                            .eTag(etag)
                            .body(appointments);
                });
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<AppointmentResponse>> getAppointmentById(@PathVariable Long id) {
        return appointmentService.getAppointmentByIdWithGarage(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<Object>> createAppointment(@Valid @RequestBody CarAppointment appointment) {
        return appointmentService.createAppointment(appointment)
                .<ResponseEntity<Object>>map(saved -> {
                    URI location = ServletUriComponentsBuilder
                            .fromCurrentRequest()
                            .path("/{id}")
                            .buildAndExpand(saved.getAppointmentId())
                            .toUri();

                    return ResponseEntity.created(location).body(saved);
                })
                .onErrorResume(IllegalArgumentException.class,
                        ex -> Mono.just(ResponseEntity.badRequest().body(ex.getMessage())));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Object>> editAppointment(@PathVariable Long id,
                                                        @Valid @RequestBody CarAppointment updatedAppointment) {
        return appointmentService.editAppointment(id, updatedAppointment)
                .<ResponseEntity<Object>>map(saved -> ResponseEntity.ok().body(saved))
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .onErrorResume(IllegalArgumentException.class,
                        ex -> Mono.just(ResponseEntity.badRequest().body(ex.getMessage())));
    }

    private String generateEtag(List<AppointmentResponse> appointments) {
        try {
            String json = objectMapper.writeValueAsString(appointments);
            return DigestUtils.md5DigestAsHex(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to generate ETag", ex);
        }
    }

    private String normalizeEtag(String rawEtag) {
        if (rawEtag == null || rawEtag.isBlank()) {
            return "";
        }

        String cleaned = rawEtag.trim();
        cleaned = cleaned.replace("W/", "");
        cleaned = cleaned.replace("\"", "");
        return cleaned;
    }
}
