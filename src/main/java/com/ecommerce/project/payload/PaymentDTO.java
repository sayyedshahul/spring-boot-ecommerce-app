package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Long paymentId;
    private String pgPaymentId;
    private String pgPaymentStatus;
    private String pgPaymentResponse;
    private String pgName;
    private String paymentMethod;
}
