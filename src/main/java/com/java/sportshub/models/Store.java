package com.java.sportshub.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Store extends Generic {

    @Column(nullable = false)
    private String name;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(unique = true)
    private String email;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    /*
     * @ManyToOne
     *
     * @JoinColumn(name = "address_id")
     */
    @Embedded
    private Address address;
}
