package com.codewithmosh.store.payment;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PaymentException extends RuntimeException{
    public PaymentException(String s) {
        super(s);
    }
}
