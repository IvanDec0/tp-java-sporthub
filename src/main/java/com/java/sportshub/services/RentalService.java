package com.java.sportshub.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.sportshub.daos.InventoryDAO;
import com.java.sportshub.daos.RentalReservationDAO;
import com.java.sportshub.dtos.RentalAvailabilityDTO;
import com.java.sportshub.exceptions.InvalidRentalPeriodException;
import com.java.sportshub.exceptions.RentalNotAvailableException;
import com.java.sportshub.exceptions.ResourceNotFoundException;
import com.java.sportshub.models.Inventory;
import com.java.sportshub.models.RentalReservation;

@Service
public class RentalService {

    @Autowired
    private InventoryDAO inventoryDAO;

    @Autowired
    private RentalReservationDAO rentalReservationDAO;

    public RentalAvailabilityDTO checkRentalAvailability(Long inventoryId, LocalDate startDate,
            LocalDate endDate, Integer requestedQuantity) {
        Inventory inventory = inventoryDAO.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", inventoryId));

        if (!"alquiler".equalsIgnoreCase(inventory.getTipo())) {
            throw new IllegalArgumentException("El inventario no es de tipo 'alquiler'");
        }

        validateRentalDates(inventory, startDate, endDate);

        Long availableQuantity = calculateAvailableQuantity(inventoryId, startDate, endDate,
                inventory.getQuantity().longValue());
        boolean isAvailable = availableQuantity >= requestedQuantity;

        String message = isAvailable
                ? "Disponible para alquiler"
                : String.format("Solo %d unidades están disponibles para las fechas solicitadas", availableQuantity);

        return new RentalAvailabilityDTO(
                inventoryId,
                startDate,
                endDate,
                requestedQuantity,
                availableQuantity,
                isAvailable,
                message);
    }

    private void validateRentalDates(Inventory inventory, LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();

        if (startDate.isBefore(today)) {
            throw new InvalidRentalPeriodException("La fecha de inicio no puede ser anterior a hoy");
        }

        if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
            throw new InvalidRentalPeriodException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        if (inventory.getAvailableFrom() != null && startDate.isBefore(inventory.getAvailableFrom())) {
            throw new InvalidRentalPeriodException(
                    String.format("El producto solo está disponible desde %s", inventory.getAvailableFrom()));
        }

        if (inventory.getAvailableUntil() != null && endDate.isAfter(inventory.getAvailableUntil())) {
            throw new InvalidRentalPeriodException(
                    String.format("El producto solo está disponible hasta %s", inventory.getAvailableUntil()));
        }

        long rentalDays = ChronoUnit.DAYS.between(startDate, endDate);

        if (inventory.getMinRentalDays() != null && rentalDays < inventory.getMinRentalDays()) {
            throw new InvalidRentalPeriodException(
                    String.format("El período mínimo de alquiler es de %d días", inventory.getMinRentalDays()));
        }

        if (inventory.getMaxRentalDays() != null && rentalDays > inventory.getMaxRentalDays()) {
            throw new InvalidRentalPeriodException(
                    String.format("El período máximo de alquiler es de %d días", inventory.getMaxRentalDays()));
        }
    }

    private Long calculateAvailableQuantity(Long inventoryId, LocalDate startDate,
            LocalDate endDate, long totalQuantity) {
        List<RentalReservation> overlappingReservations = rentalReservationDAO.findOverlappingReservations(inventoryId,
                startDate, endDate);

        long maxReservedQuantity = 0;
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            final LocalDate checkDate = currentDate;
            long reservedForDate = overlappingReservations.stream()
                    .filter(r -> !checkDate.isBefore(r.getStartDate()) && !checkDate.isAfter(r.getEndDate()))
                    .mapToLong(RentalReservation::getQuantity)
                    .sum();

            maxReservedQuantity = Math.max(maxReservedQuantity, reservedForDate);
            currentDate = currentDate.plusDays(1);
        }

        return Math.max(0, totalQuantity - maxReservedQuantity);
    }

    @Transactional
    public RentalReservation createRentalReservation(RentalReservation reservation) {
        Inventory inventory = reservation.getInventory();

        // Validar disponibilidad
        RentalAvailabilityDTO availability = checkRentalAvailability(
                inventory.getId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getQuantity());

        if (!availability.getIsAvailable()) {
            throw new RentalNotAvailableException(availability.getMessage());
        }

        // Calcular precio total
        long rentalDays = ChronoUnit.DAYS.between(reservation.getStartDate(), reservation.getEndDate());
        double totalPrice = inventory.getPricePerDay() * rentalDays * reservation.getQuantity();
        reservation.setTotalPrice(totalPrice);

        // Estado inicial
        if (reservation.getStatus() == null) {
            reservation.setStatus("PENDING");
        }

        return rentalReservationDAO.save(reservation);
    }

    @Transactional
    public void cancelReservation(Long reservationId) {
        RentalReservation reservation = rentalReservationDAO.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("RentalReservation", "id", reservationId));

        if ("COMPLETED".equals(reservation.getStatus()) || "CANCELLED".equals(reservation.getStatus())) {
            throw new IllegalStateException("No se puede cancelar una reserva que ya está completada o cancelada");
        }

        reservation.setStatus("CANCELLED");
        reservation.setIsActive(false);
        rentalReservationDAO.save(reservation);
    }

    @Transactional
    public RentalReservation confirmReservation(Long reservationId) {
        RentalReservation reservation = rentalReservationDAO.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("RentalReservation", "id", reservationId));

        if (!"PENDING".equals(reservation.getStatus())) {
            throw new IllegalStateException("Solo se pueden confirmar reservas en estado PENDING");
        }

        // Re-validar disponibilidad antes de confirmar
        RentalAvailabilityDTO availability = checkRentalAvailability(
                reservation.getInventory().getId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getQuantity());

        if (!availability.getIsAvailable()) {
            throw new RentalNotAvailableException("El inventario no está disponible para estas fechas");
        }

        reservation.setStatus("CONFIRMED");
        return rentalReservationDAO.save(reservation);
    }

    @Transactional
    public RentalReservation activateReservation(Long reservationId) {
        RentalReservation reservation = rentalReservationDAO.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("RentalReservation", "id", reservationId));

        if (!"CONFIRMED".equals(reservation.getStatus())) {
            throw new IllegalStateException("Solo se pueden activar reservas confirmadas");
        }

        reservation.setStatus("ACTIVE");
        return rentalReservationDAO.save(reservation);
    }

    @Transactional
    public RentalReservation completeReservation(Long reservationId) {
        RentalReservation reservation = rentalReservationDAO.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("RentalReservation", "id", reservationId));

        if (!"ACTIVE".equals(reservation.getStatus())) {
            throw new IllegalStateException("Solo se pueden completar reservas activas");
        }

        reservation.setStatus("COMPLETED");
        return rentalReservationDAO.save(reservation);
    }

    public List<RentalReservation> getActiveReservations(Long inventoryId) {
        return rentalReservationDAO.findByInventoryIdAndIsActiveTrue(inventoryId);
    }

    public void validateStockForSale(Long inventoryId, int quantity) {
        Inventory inventory = inventoryDAO.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", inventoryId));

        if (!"venta".equalsIgnoreCase(inventory.getTipo())) {
            throw new IllegalArgumentException("Este inventario no es para venta");
        }

        if (inventory.getQuantity() < quantity) {
            throw new RentalNotAvailableException(
                    String.format("Stock insuficiente. Disponible: %d, Requerido: %d",
                            inventory.getQuantity(), quantity));
        }
    }
}
