package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @OneToOne(mappedBy = "payment")
    private Order order;

    private String pgPaymentId;
    private String pgPaymentStatus;
    private String pgPaymentResponse;
    private String pgName;

    @NotBlank
    @Size(min = 4, message = "Payment method must be at least 4 characters")
    private String paymentMethod;

    public Payment(String paymentMethod, String pgPaymentStatus, String pgPaymentId, String pgPaymentResponse, String pgName) {
        this.pgPaymentStatus = pgPaymentStatus;
        this.pgPaymentId = pgPaymentId;
        this.pgPaymentResponse = pgPaymentResponse;
        this.pgName = pgName;
        this.paymentMethod = paymentMethod;
    }
}
