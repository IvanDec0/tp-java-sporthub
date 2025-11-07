package com.java.sportshub.models;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

// @Entity
@Getter
@Setter
@Embeddable // This is an annotation that indicates that this class is an embeddable class.
// Embeddable: A class that is used as a field in another entity class (Not a
// relationship).
public class Address {
    /*
     * @Id
     *
     * @GeneratedValue(strategy = GenerationType.AUTO)
     * private long id;
     */

    private String country;
    private String state;
    private String city;
    private String street;
    private String number;
}
