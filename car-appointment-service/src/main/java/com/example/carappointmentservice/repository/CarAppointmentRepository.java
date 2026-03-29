package com.example.carappointmentservice.repository;

import com.example.carappointmentservice.entity.CarAppointment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarAppointmentRepository extends JpaRepository<CarAppointment, Long> {
}
