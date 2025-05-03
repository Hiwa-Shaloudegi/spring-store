package com.codewithmosh.store.payments;

import com.codewithmosh.store.common.ErrorDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RequiredArgsConstructor
@RestController
@RequestMapping("/checkout")
public class CheckoutController {


    private final CheckoutService checkoutService;

    @PostMapping
    public ResponseEntity<?> checkout(@Valid @RequestBody CheckoutRequest request) {
        CheckoutResponse checkout = checkoutService.checkout(request);
        return ResponseEntity.ok(checkout);

    }


    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestHeader Map<String, String> headers, @RequestBody String payload
    ) {
        //        var signature = headers.get("Stripe-Signature");
        checkoutService.handleWebhookEvent(new WebhookRequest(headers, payload));
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ErrorDto> handlePaymentException(PaymentException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorDto(ex.getMessage()));
    }

}
