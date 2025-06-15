package com.codewithmosh.store.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "date_created", insertable = false, updatable = false)
    private LocalDate dateCreated;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.MERGE, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<CartItem> cartItems = new LinkedHashSet<>();

    public BigDecimal getTotalPrice() {
        BigDecimal totalPrice = cartItems.stream()
                .map(item -> {
                    return item.getTotalPrice();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalPrice;
    }

    public CartItem getItem(Long productId) {
        return getCartItems().stream()
                .filter(item -> {
                    return item.getProduct().getId().equals(productId);
                })
                .findFirst().orElse(null);
    }

    public CartItem addItem(Product product) {
        //判断这个商品item是否已经在购物车中了，如果已经存在, 只增加商品数量
        CartItem cartItem = getItem(product.getId());
        if (cartItem != null) { //购物车中已经有这个商品了，只更改数量字段
            cartItem.setQuantity(cartItem.getQuantity() + 1);
        } else { //不存在，说明是第一次添加这个商品到购物车
            cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(1);
            cartItem.setCart(this);
            cartItems.add(cartItem);
        }
        return cartItem;
    }

    public void removeItem(Long productId) {
        CartItem cartItem = getItem(productId);
        if (cartItem != null) {
            cartItems.remove(cartItem);
            //cartItem.setCart(null);
        }
    }

    public void clearItems() {
        cartItems.clear();
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }
}
