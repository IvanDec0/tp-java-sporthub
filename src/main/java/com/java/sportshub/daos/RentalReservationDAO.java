package com.java.sportshub.daos;

import com.java.sportshub.models.RentalReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RentalReservationDAO extends JpaRepository<RentalReservation, Long> {

    @Query("SELECT r FROM RentalReservation r WHERE r.inventory.id = :inventoryId " +
            "AND r.isActive = true " +
            "AND r.status IN ('PENDING', 'CONFIRMED', 'ACTIVE') " +
            "AND ((r.startDate <= :endDate AND r.endDate >= :startDate))")
    List<RentalReservation> findOverlappingReservations(
            @Param("inventoryId") Long inventoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM RentalReservation r " +
            "WHERE r.inventory.id = :inventoryId " +
            "AND r.isActive = true " +
            "AND r.status IN ('PENDING', 'CONFIRMED', 'ACTIVE') " +
            "AND r.startDate <= :date AND r.endDate >= :date")
    Long countReservedQuantityForDate(
            @Param("inventoryId") Long inventoryId,
            @Param("date") LocalDate date);

    List<RentalReservation> findByInventoryIdAndIsActiveTrue(Long inventoryId);

    List<RentalReservation> findByUserIdAndStatusInAndIsActiveTrue(Long userId, List<String> statuses);
}
