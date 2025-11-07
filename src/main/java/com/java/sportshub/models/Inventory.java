package com.java.sportshub.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Inventory extends Generic {

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private String tipo; // 'Venta' o 'Alquiler'

    // Campos específicos para alquiler
    @Column(name = "price_per_day")
    private Double pricePerDay; // Precio por día de alquiler

    @Column(name = "min_rental_days")
    private Integer minRentalDays; // Días mínimos de alquiler

    @Column(name = "max_rental_days")
    private Integer maxRentalDays; // Días máximos de alquiler

    @Column(name = "available_from")
    private LocalDate availableFrom; // Fecha desde la cual está disponible

    @Column(name = "available_until")
    private LocalDate availableUntil; // Fecha hasta la cual está disponible

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RentalReservation> reservations = new ArrayList<>();
}
