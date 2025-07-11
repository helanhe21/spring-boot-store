package com.codewithmosh.store.dtos;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemDto {
    private CartProductDto productDto;
    private Integer quantity;
    private BigDecimal totalPrice;
}
