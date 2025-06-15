package com.codewithmosh.store.services;

import com.codewithmosh.store.dtos.CartDto;
import com.codewithmosh.store.dtos.CartItemDto;
import com.codewithmosh.store.entities.Cart;
import com.codewithmosh.store.entities.CartItem;
import com.codewithmosh.store.entities.Product;
import com.codewithmosh.store.exceptions.CartNotFoundException;
import com.codewithmosh.store.exceptions.ProductNotFoundException;
import com.codewithmosh.store.mappers.CartMapper;
import com.codewithmosh.store.repositories.CartRepository;
import com.codewithmosh.store.repositories.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;
    private final ProductRepository productRepository;

    public CartDto createCart() {
        Cart cart = new Cart();
        cartRepository.save(cart);

        return cartMapper.toDto(cart);
    }

    public CartItemDto addToCart(UUID cartId, Long productId) {
        //先验证数据库中是否有相关数据, 首先需要找到购物车对象是否存在？
        Cart cart = cartRepository.findById(cartId).orElse(null);
        if (cart == null) {
            throw new CartNotFoundException("Cart is not found.");
        }
        //验证添加的商品是否在数据库中存在？
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            //return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            throw new ProductNotFoundException();
        }

        //添加商品到购物车中了，如果商品已经存在, 只增加商品数量
        CartItem cartItem = cart.addItem(product);

        //保存或更新到数据库的carts表中，这里会级联保存购物车商品到cart_items表
        cartRepository.save(cart);

        return cartMapper.toCartItemDto(cartItem);
    }

    public CartDto getCart(UUID cartId) {
        Cart cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart == null) {
            throw new CartNotFoundException("Cart is not found.");
        }

        return cartMapper.toDto(cart);
    }

    public CartItemDto updateCartItem(UUID cartId,
                                      Long productId,
                                      Integer quantity) {
        //验证ID为cartId的购物车是否存在
        Cart cart = cartRepository.getCartWithItems(cartId).orElse(null);
        if (cart == null) {
            throw new CartNotFoundException("Cart is not found.");
        }

        //根据productId查找购物车中的某个商品，如果商品不存在返回错误提示
        CartItem cartItem = cart.getItem(productId);
        if (cartItem == null) {
            throw new ProductNotFoundException();
        }

        cartItem.setQuantity(quantity);
        cartRepository.save(cart);
        return cartMapper.toCartItemDto(cartItem);
    }

    public void removeItem(UUID cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId).orElse(null);
        if (cart == null) {
            throw new CartNotFoundException("Cart is not found.");
        }

        cart.removeItem(productId);
        cartRepository.save(cart);
    }

    public void clearItems(UUID cartId) {
        Cart cart = cartRepository.findById(cartId).orElse(null);
        if (cart == null) {
            throw new CartNotFoundException("Cart is not found.");
        }

        cart.clearItems();
        cartRepository.save(cart);
    }
}
