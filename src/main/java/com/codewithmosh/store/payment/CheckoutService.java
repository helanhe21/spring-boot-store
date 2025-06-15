package com.codewithmosh.store.payment;

import com.codewithmosh.store.entities.Cart;
import com.codewithmosh.store.entities.Order;
import com.codewithmosh.store.exceptions.CartIsEmptyException;
import com.codewithmosh.store.exceptions.CartNotFoundException;
import com.codewithmosh.store.repositories.CartRepository;
import com.codewithmosh.store.repositories.OrderRepository;
import com.codewithmosh.store.services.AuthService;
import com.codewithmosh.store.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckoutService {
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final AuthService authService;
    private final CartService cartService;
    private final PaymentGateway paymentGateway;

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        Cart cart = cartRepository.getCartWithItems(request.getCartId()).orElse(null);
        //如果购物车不存在，抛异常
        if (cart == null) {
            throw new CartNotFoundException("Cart is not found.");
        }

        //如果购物车中没有商品，抛异常
        if (cart.isEmpty()) {
            throw new CartIsEmptyException("Cart is empty.");
        }

        Order order = Order.fromCart(cart, authService.getCurrentUser());

        //保存订单信息到数据库
        orderRepository.save(order);

        try {
            //创建Checkout session
            CheckoutSession checkoutSession = paymentGateway.createCheckoutSession(order);

            //清空购物车
            cartService.clearItems(cart.getId());

            return new CheckoutResponse(order.getId(), checkoutSession.getCheckoutUrl());
        }
        catch (PaymentException ex) {
            orderRepository.delete(order);
            throw ex;
        }
    }

    public void handleWebhookEvent(WebhookRequest webhookRequest) {
        paymentGateway
                .parseWebhookRequest(webhookRequest)
                .ifPresent(paymentResult -> {
                    Order order = orderRepository.findById(paymentResult.getOrderId()).orElseThrow();
                    order.setStatus(paymentResult.getPaymentStatus());
                    orderRepository.save(order);
                });
    }
}
