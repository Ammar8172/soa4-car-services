package com.example.garageservice.controller;

import com.example.garageservice.entity.Garage;
import com.example.garageservice.repository.GarageRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/garages")
public class GarageController {

    private final GarageRepository garageRepository;

    public GarageController(GarageRepository garageRepository) {
        this.garageRepository = garageRepository;
    }

    @GetMapping
    public List<Garage> getAllGarages() {
        return garageRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Garage> getGarageById(@PathVariable Long id) {
        Optional<Garage> garage = garageRepository.findById(id);
        return garage.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Garage> createGarage(@Valid @RequestBody Garage garage) {
        garage.setGarageId(null);
        Garage saved = garageRepository.save(garage);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getGarageId())
                .toUri();

        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Garage> editGarage(@PathVariable Long id, @Valid @RequestBody Garage updatedGarage) {
        Optional<Garage> existing = garageRepository.findById(id);

        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Garage garage = existing.get();
        garage.setGarageName(updatedGarage.getGarageName());
        garage.setLocation(updatedGarage.getLocation());
        garage.setSpeciality(updatedGarage.getSpeciality());
        garage.setPhoneNumber(updatedGarage.getPhoneNumber());

        Garage saved = garageRepository.save(garage);
        return ResponseEntity.ok(saved);
    }
}
