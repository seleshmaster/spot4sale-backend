package com.spot4sale.controller;

import com.spot4sale.entity.Booking;
import com.spot4sale.repository.BookingRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class StripeWebhookController {

    private final BookingRepository bookings;
    private final String webhookSecret;

    public StripeWebhookController(BookingRepository bookings,
                                   @Value("${stripe.webhook-secret:}") String webhookSecret) {
        this.bookings = bookings;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/webhook")
    @ResponseStatus(HttpStatus.OK)
    public void handle(@RequestHeader("Stripe-Signature") String sig, @RequestBody String payload) throws Exception {
        Event event;
        if (webhookSecret != null && !webhookSecret.isBlank()) {
            try {
                event = Webhook.constructEvent(payload, sig, webhookSecret);
            } catch (SignatureVerificationException e) {
                throw new IllegalArgumentException("Invalid Stripe signature");
            }
        } else {
            // dev only (no signature verification)
            event = Event.GSON.fromJson(payload, Event.class);
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent pi = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
            if (pi != null && pi.getMetadata() != null) {
                String bookingId = pi.getMetadata().get("bookingId");
                if (bookingId != null) {
                    bookings.findById(UUID.fromString(bookingId)).ifPresent(b -> {
                        b.setStatus("PAID");
                        bookings.save(b);
                    });
                }
            }
        }
    }
}
