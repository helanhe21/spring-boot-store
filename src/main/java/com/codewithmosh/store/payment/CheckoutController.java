package com.codewithmosh.store.payment;

import com.codewithmosh.store.dtos.ErrorDto;
import com.codewithmosh.store.exceptions.CartIsEmptyException;
import com.codewithmosh.store.exceptions.CartNotFoundException;
import com.codewithmosh.store.repositories.OrderRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {
    private final CheckoutService checkoutService;
    private final OrderRepository orderRepository;

    @PostMapping
    public CheckoutResponse checkout(
            @Valid @RequestBody CheckoutRequest request) {
        return checkoutService.checkout(request);
    }

    @PostMapping("/webhook")
    public void handleWebhook(
            @RequestHeader Map<String, String> headers,
            @RequestBody String payload
    ) {
        checkoutService.handleWebhookEvent(new WebhookRequest(headers, payload));
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorDto> handlePaymentException() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDto("Error creating a checkout session."));
    }

    @ExceptionHandler({CartNotFoundException.class, CartIsEmptyException.class})
    public ResponseEntity<ErrorDto> handleException(Exception exception) {
        return ResponseEntity.badRequest().body(new ErrorDto(exception.getMessage()));
    }
}
