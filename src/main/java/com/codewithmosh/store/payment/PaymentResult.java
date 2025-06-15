package com.codewithmosh.store.payment;

import com.codewithmosh.store.entities.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PaymentResult {
    private Long orderId;
    private OrderStatus paymentStatus;
}
