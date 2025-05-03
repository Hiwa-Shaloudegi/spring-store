package com.codewithmosh.store.payments;

import com.codewithmosh.store.orders.Order;
import com.codewithmosh.store.orders.OrderItem;
import com.codewithmosh.store.orders.PaymentStatus;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
public class StripePaymentGatewayImpl implements PaymentGateway {
    @Value("${websiteUrl}")
    private String websiteUrl;

    @Value("${stripe.webhookSecretKey}")
    private String webhookSecret;

    @Override
    public CheckoutSession createCheckoutSession(Order order) {
        try {
            var builder = SessionCreateParams
                    .builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(websiteUrl + "/checkout-success.html?orderId=" + order.getId())
                    .setCancelUrl(websiteUrl + "/checkout-cancel")
                    .putMetadata("order_id", order.getId().toString());
            order.getItems().forEach(item -> {
                builder.addLineItem(getLineItem(item));
            });

            var session = Session.create(builder.build());
            return new CheckoutSession(session.getUrl());

        } catch (StripeException ex) {
            log.error("Error creating Stripe checkout session:", ex);
            throw new PaymentException();
        }
    }

    @Override
    public Optional<PaymentResult> parseWebhookRequest(WebhookRequest request) {
        try {
            var signature = request.getHeaders().get("stripe-signature");
            var event = Webhook.constructEvent(request.getPayload(), signature, webhookSecret);
            System.out.println(event.getType());

            // charge -> (Charge) stripeObject;
            // payment_intent.succeeded -> (PaymentIntent) stripeObject;
            switch (event.getType()) {
                // Update order status to (PAID)
                case "payment_intent.succeeded" -> {
                    Long orderId = extractOrderId(event);
                    return Optional.of(new PaymentResult(orderId, PaymentStatus.PAID));
                }

                // Update order status to (FAILED)
                case "payment_intent.payment_failed" -> {
                    Long orderId = extractOrderId(event);
                    return Optional.of(new PaymentResult(orderId, PaymentStatus.FAILED));
                }

                default -> {
                    log.warn("Unhandled event type: {}", event.getType());
                    return Optional.empty();
                }
            }

        } catch (SignatureVerificationException e) {
            throw new PaymentException("Invalid signature");
        }

    }

    private Long extractOrderId(Event event) {
        var stripeObject = event
                .getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new PaymentException("Failed to deserialize event data"));
        var paymentIntent = (PaymentIntent) stripeObject;
        return Long.valueOf(paymentIntent.getMetadata().get("order_id"));

    }

    private SessionCreateParams.LineItem getLineItem(OrderItem item) {
        return SessionCreateParams.LineItem
                .builder()
                .setQuantity(Long.valueOf(item.getQuantity()))
                .setPriceData(getPriceData(item))
                .build();
    }

    private SessionCreateParams.LineItem.PriceData getPriceData(OrderItem item) {
        return SessionCreateParams.LineItem.PriceData
                .builder()
                .setCurrency("usd")
                .setUnitAmountDecimal(item.getUnitPrice().multiply(BigDecimal.valueOf(100))) // in cent not dollar
                .setProductData(getProductData(item))
                .build();
    }

    private SessionCreateParams.LineItem.PriceData.ProductData getProductData(OrderItem item) {
        return SessionCreateParams.LineItem.PriceData.ProductData
                .builder()
                .setName(item.getProduct().getName())
                .build();
    }
}
