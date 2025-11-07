package com.java.sportshub.controllers;

import com.java.sportshub.dtos.RentalAvailabilityDTO;
import com.java.sportshub.dtos.RentalReservationDTO;
import com.java.sportshub.models.RentalReservation;
import com.java.sportshub.services.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    @Autowired
    private RentalService rentalService;


    @GetMapping("/availability")
    public ResponseEntity<RentalAvailabilityDTO> checkAvailability(
            @RequestParam Long inventoryId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam Integer quantity) {

        RentalAvailabilityDTO availability = rentalService.checkRentalAvailability(
                inventoryId, startDate, endDate, quantity);
        return ResponseEntity.ok(availability);
    }


    @GetMapping("/inventory/{inventoryId}")
    public ResponseEntity<List<RentalReservationDTO>> getActiveReservations(@PathVariable Long inventoryId) {
        List<RentalReservation> reservations = rentalService.getActiveReservations(inventoryId);
        List<RentalReservationDTO> dtos = reservations.stream()
                .map(RentalReservationDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }


    @PostMapping("/reservations/{id}/confirm")
    public ResponseEntity<RentalReservationDTO> confirmReservation(@PathVariable Long id) {
        RentalReservation confirmed = rentalService.confirmReservation(id);
        return ResponseEntity.ok(new RentalReservationDTO(confirmed));
    }


    @PostMapping("/reservations/{id}/activate")
    public ResponseEntity<RentalReservationDTO> activateReservation(@PathVariable Long id) {
        RentalReservation activated = rentalService.activateReservation(id);
        return ResponseEntity.ok(new RentalReservationDTO(activated));
    }


    @PostMapping("/reservations/{id}/complete")
    public ResponseEntity<RentalReservationDTO> completeReservation(@PathVariable Long id) {
        RentalReservation completed = rentalService.completeReservation(id);
        return ResponseEntity.ok(new RentalReservationDTO(completed));
    }


    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        rentalService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }
}
