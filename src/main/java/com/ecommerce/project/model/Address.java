package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "address")
@Getter
@Setter
public class Address {
    @Id
    @GeneratedValue
    @Column(name = "address_id")
    private Long addressId;

    @NotBlank
    @Size(min = 5, message = "Building name must be atleast 5 characters")
    private String buildingName;

    @NotBlank
    @Size(min = 4, message = "City name must be atleast 4 characters")
    private String city;

    @NotBlank
    @Size(min = 2, message = "State name must be atleast 2 characters")
    private String state;

    @NotBlank
    @Size(min = 2, message = "Country name must be atleast 2 characters")
    private String country;

    @NotBlank
    @Size(min = 5, message = "Pincode must be atleast 5 characters")
    private String pincode;

    @ToString.Exclude
    @ManyToMany(mappedBy = "addresses")
    private Set<User> users = new HashSet<>();
}
