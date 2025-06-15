package com.codewithmosh.store.payment;

import com.codewithmosh.store.entities.Order;
import com.codewithmosh.store.entities.OrderItem;
import com.codewithmosh.store.entities.OrderStatus;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StripePaymentGateway implements PaymentGateway{
    @Value("${websiteUrl}")
    private String websiteUrl;
    @Value("${stripe.webhookSecretKey}")
    private String webhookSecretKey;

    @Override
    public CheckoutSession createCheckoutSession(Order order) {
        try {
            //create a Stripe checkout session,首先创建一个SessionCreateParams builder
            SessionCreateParams.Builder sessionParamsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(websiteUrl + "/checkout-success?orderId=" + order.getId())
                    .setCancelUrl(websiteUrl + "/checkout-cancel")
                    .putMetadata("order_id", order.getId().toString());
            //将订单中所有的商品信息加入到sessionParamsBuilder中
            order.getItems().forEach(item -> {
                SessionCreateParams.LineItem lineItem = createLineItem(item);
                sessionParamsBuilder.addLineItem(lineItem);
            });
            //构建出Session Params对象
            SessionCreateParams sessionCreateParams = sessionParamsBuilder.build();
            //使用session params创建Stripe Session对象
            Session session = Session.create(sessionCreateParams);

            return new CheckoutSession(session.getUrl());
        }
        catch (StripeException ex) {
            System.out.println(ex.getMessage());
            throw new PaymentException();
        }
    }

    @Override
    public Optional<PaymentResult> parseWebhookRequest(WebhookRequest request) {
        //解析payload
        try {
            var payload = request.getPayload();
            var signature = request.getHeaders().get("stripe-signature");
            Event event = Webhook.constructEvent(payload, signature, webhookSecretKey);

            switch(event.getType()) {
                case "payment_intent.succeeded" -> {
                    //update order status (PAID)
                    return Optional.of(new PaymentResult(extractOrderId(event),OrderStatus.PAID));
                }
                case "payment_intent:payment_failed" -> {
                    //update order status (FAILED)
                    return Optional.of(new PaymentResult(extractOrderId(event),OrderStatus.FAILED));
                }
                default -> {
                    return Optional.empty();
                }
            }
        } catch (SignatureVerificationException e) {
            //解析signature不正确，返回异常供Controller处理
            throw new PaymentException("Invalid signature.");
        }
    }

    private Long extractOrderId(Event event) {
        //我们可以从event对象中获取状态和数据，告我我们发生了什么
        StripeObject stripeObject = event.getDataObjectDeserializer().getObject()
                .orElseThrow(() -> new PaymentException("Couldn't deserialize Stripe event."));
        var paymentIntent = (PaymentIntent)stripeObject;
        String orderId = paymentIntent.getMetadata().get("order_id");
        return Long.valueOf(orderId);
    }

    private SessionCreateParams.LineItem createLineItem(OrderItem item) {
        return SessionCreateParams.LineItem.builder()
                .setQuantity(Long.valueOf(item.getQuantity()))
                .setPriceData(createPriceData(item))
                .build();
    }

    private SessionCreateParams.LineItem.PriceData createPriceData(OrderItem item) {
        return SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency("usd")
                .setUnitAmountDecimal(item.getUnitPrice())//注意这里的价格单位是美分
                .setProductData(createProductData(item))
                .build();
    }

    private SessionCreateParams.LineItem.PriceData.ProductData createProductData(OrderItem item) {
        return SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(item.getProduct().getName()).build();
    }
}
