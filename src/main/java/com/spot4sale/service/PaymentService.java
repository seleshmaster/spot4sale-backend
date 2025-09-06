package com.spot4sale.service;

import com.spot4sale.config.StripeProperties;
import com.spot4sale.dto.InitPaymentRequest;
import com.spot4sale.entity.Booking;
import com.spot4sale.entity.Booth;
import com.spot4sale.entity.Host;
import com.spot4sale.entity.User;
import com.spot4sale.repository.BookingRepository;
import com.spot4sale.repository.BoothRepository;
import com.spot4sale.repository.HostRepository;
import com.spot4sale.repository.UserRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private final BookingRepository bookings;
    private final BoothRepository spots;
    private final HostRepository stores;
    private final UserRepository users;
    private final StripeProperties props;
    private final BookingService bookingService; // to mark as PAID, etc.

    public PaymentService(BookingRepository bookings,
                          BoothRepository spots,
                          HostRepository stores,
                          UserRepository users,
                          StripeProperties props,
                          BookingService bookingService) {
        this.bookings = bookings;
        this.spots = spots;
        this.stores = stores;
        this.users = users;
        this.props = props;
        this.bookingService = bookingService;
    }

    @PostConstruct
    void initStripe() {
        com.stripe.Stripe.apiKey = props.getSecretKey(); // or getApiKey(), whatever your StripeProperties exposes
    }

    /** Create PaymentIntent for a booking (keeps your current API/shape) */
    /** Create PaymentIntent for a booking (keeps your current API/shape) */
    @Transactional // <-- remove readOnly=true so we can update the booking
    public Map<String, String> createIntentForBooking(InitPaymentRequest req, Authentication auth) {
        User user = AuthUtils.requireUser(users, auth);

        Booking b = bookings.findById(req.bookingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!b.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your booking");
        }
        if (b.getTotalPrice() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking has no total");
        }

        Booth spot = spots.findById(b.getSpotId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Spot not found"));
        Host store = stores.findById(spot.getStoreId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        User owner = users.findById(store.getOwnerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found"));

        long amountCents = toCents(b.getTotalPrice());
        long appFeeCents = Math.max(100, Math.round(amountCents * (props.getAppFeePercent() / 100.0)));

        try {
            PaymentIntentCreateParams.Builder builder = PaymentIntentCreateParams.builder()
                    .setAmount(amountCents)
                    .setCurrency("usd")
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build()
                    )
                    .putMetadata("bookingId", b.getId().toString());

            if (props.isConnectEnabled()
                    && owner.getStripeAccountId() != null && !owner.getStripeAccountId().isBlank()) {
                builder
                        .setApplicationFeeAmount(appFeeCents)
                        .setTransferData(
                                PaymentIntentCreateParams.TransferData.builder()
                                        .setDestination(owner.getStripeAccountId())
                                        .build()
                        );
            }

            PaymentIntent pi = PaymentIntent.create(builder.build());

            // 👇 persist the PI id so refunds/cancellations can find it later
            b.setPaymentIntentId(pi.getId());
            bookings.save(b);

            return Map.of("clientSecret", pi.getClientSecret());
        } catch (StripeException se) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stripe error: " + se.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Stripe error: " + e.getMessage());
        }
    }

    /** Verify webhook signature, mark bookings as PAID on success */
    @Transactional
    public void handleWebhook(String payload, String signatureHeader) {
        final String secret = props.getWebhookSecret();
        final Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, secret);
        } catch (SignatureVerificationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Stripe signature");
        }

        final String type = event.getType();
        // log type to verify you're receiving it
        // log.info("Stripe event: {}", type);

        if ("payment_intent.succeeded".equals(type)) {
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

            PaymentIntent pi = deserializer.getObject()
                    .map(obj -> (PaymentIntent) obj)
                    .orElseGet(() -> {
                        // Fallback: deserialize from raw JSON (not expanded object)
                        String raw = deserializer.getRawJson();      // available in stripe-java 20+
                        return ApiResource.GSON.fromJson(raw, PaymentIntent.class);
                    });

            if (pi != null && pi.getMetadata() != null) {
                String bookingIdStr = pi.getMetadata().get("bookingId");
                if (bookingIdStr != null && !bookingIdStr.isBlank()) {
                    UUID bookingId = UUID.fromString(bookingIdStr);
                    bookingService.markPaid(bookingId); // will now run
                }
            }
        }

        // handle other events as needed
    }

    private static long toCents(BigDecimal dollars) {
        return dollars.movePointRight(2).setScale(0, java.math.RoundingMode.HALF_UP).longValueExact();
    }
}
