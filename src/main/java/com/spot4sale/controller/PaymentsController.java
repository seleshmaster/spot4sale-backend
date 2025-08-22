package com.spot4sale.controller;

import com.spot4sale.dto.InitPaymentRequest;
import com.spot4sale.entity.Booking;
import com.spot4sale.entity.Spot;
import com.spot4sale.entity.Store;
import com.spot4sale.entity.User;
import com.spot4sale.repository.BookingRepository;
import com.spot4sale.repository.SpotRepository;
import com.spot4sale.repository.StoreRepository;
import com.spot4sale.repository.UserRepository;
import com.spot4sale.service.AuthUtils;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import com.stripe.exception.StripeException;
import com.stripe.param.PaymentIntentCreateParams;

@RestController
@RequestMapping("/api/payments")
public class PaymentsController {

    private final BookingRepository bookings;
    private final SpotRepository spots;
    private final StoreRepository stores;
    private final UserRepository users;

    @Value("${stripe.connect.enabled:true}")
    private boolean connectEnabled;

    public PaymentsController(
            BookingRepository bookings,
            SpotRepository spots,
            StoreRepository stores,
            UserRepository users,
            @Value("${stripe.secret-key}") String stripeSecret
    ) {
        this.bookings = bookings;
        this.spots = spots;
        this.stores = stores;
        this.users = users;
        Stripe.apiKey = stripeSecret;
    }

    /** Create a PaymentIntent for a booking and return client_secret */
    @PostMapping("/intent")
    public Map<String, String> createIntent(@RequestBody InitPaymentRequest req, Authentication auth) {
        var user = AuthUtils.requireUser(users, auth);

        Booking b = bookings.findById(req.bookingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!b.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your booking");
        }
        if (b.getTotalPrice() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking has no total");
        }

        Spot spot = spots.findById(b.getSpotId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Spot not found"));
        Store store = stores.findById(spot.getStoreId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));

        User owner = users.findById(store.getOwnerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found"));

        long amountCents = toCents(b.getTotalPrice());
        long appFeeCents = Math.max(100, Math.round(amountCents * 0.10)); // example 10% fee, min $1

        try {
            PaymentIntentCreateParams.Builder builder = PaymentIntentCreateParams.builder()
                    .setAmount(amountCents)
                    .setCurrency("usd")
                    // IMPORTANT for Payment Element:
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods
                                    .builder().setEnabled(true).build())
                    .putMetadata("bookingId", b.getId().toString());

            // If testing Connect revenue share, ensure you have a TEST connected account:
            if (owner.getStripeAccountId() != null && !owner.getStripeAccountId().isBlank()) {
                builder
                        .setApplicationFeeAmount(appFeeCents)
                        .setTransferData(
                                PaymentIntentCreateParams.TransferData
                                        .builder().setDestination(owner.getStripeAccountId()).build());
            } else {
                // Optional: fail fast, or comment this to test platform-only payments
                // throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Store is not onboarded for payouts");
                // For platform-only local testing (no Connect), just proceed without transfer/app fee.
            }

            PaymentIntent pi = PaymentIntent.create(builder.build());
            return Map.of("clientSecret", pi.getClientSecret());

        } catch (StripeException se) {
            // Bubble up Stripe’s message to the client as a 400 for easier debugging
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stripe error: " + se.getMessage());
        } catch (Exception e) {
            // Unexpected server-side problems
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Stripe error: " + e.getMessage());
        }
    }

    private static long toCents(BigDecimal dollars) {
        return dollars.movePointRight(2).setScale(0, java.math.RoundingMode.HALF_UP).longValueExact();
    }
}
