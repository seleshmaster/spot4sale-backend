package com.spot4sale.service;

import com.spot4sale.dto.BookingDetailsDto;
import com.spot4sale.dto.SpotSummary;
import com.spot4sale.dto.StoreSummary;
import com.spot4sale.entity.Booking;
import com.spot4sale.entity.Spot;
import com.spot4sale.entity.Store;
import com.spot4sale.entity.User;
import com.spot4sale.repository.BookingRepository;
import com.spot4sale.repository.SpotRepository;
import com.spot4sale.repository.StoreRepository;
import com.spot4sale.repository.UserRepository;
import com.spot4sale.service.AuthUtils;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.temporal.ChronoUnit;

import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookings;
    private final SpotRepository spots;
    private final StoreRepository stores;
    private final UserRepository users;
    private final AuthUtils authUtils;

    public BookingService(BookingRepository bookings, SpotRepository spots, StoreRepository stores, UserRepository users, AuthUtils authUtils) {
        this.bookings = bookings;
        this.spots = spots;
        this.stores = stores;
        this.users = users;
        this.authUtils = authUtils;
    }

    /**
     * Create a booking for the authenticated user.
     */
    // backend/src/main/java/com/spot4sale/service/BookingService.java
    @Transactional
    public Booking create(UUID spotId, LocalDate start, LocalDate end, Authentication auth) {
        User u = AuthUtils.requireUser(users, auth);

        // 1) Basic date sanity
        if (start == null || end == null || !start.isBefore(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date range");
        }

        // Optional: garage sales are 1–2 days max (from your spec)
        long nights = ChronoUnit.DAYS.between(start, end);
        if (nights < 1 || nights > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Garage sales must be 1–2 days");
        }

        // Optional: no past bookings
        if (start.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be in the past");
        }

        // 2) Spot exists and is available
        Spot spot = spots.findById(spotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Spot not found"));
        if (spot.getAvailable() == null || !spot.getAvailable()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Spot is not available");
        }

        // 3) Quick overlap check before DB constraint (for friendlier error)
        long overlaps = bookings.countOverlapping(spotId, start, end);
        if (overlaps > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This spot is already booked for those dates");
        }

        // 4) Compute total (pricePerDay * nights)
        BigDecimal total = BigDecimal.valueOf((spot.getPricePerDay() != null ? spot.getPricePerDay() : 0.0) * nights);

        // 5) Create and save
        Booking b = new Booking();
        b.setUserId(u.getId());
        b.setSpotId(spot.getId());
        b.setStartDate(start);
        b.setEndDate(end);
        b.setStatus("PENDING"); // or "PENDING" if you want payment-first
        b.setTotalPrice(total);

        long mine = bookings.countUserOverlap(u.getId(), spotId, start, end);
        if (mine > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have a booking for this spot in that window.");
        }

        try {
            return bookings.saveAndFlush(b); // flush so DB constraint violations throw here
        } catch (DataIntegrityViolationException ex) {
            // Catch the EXCLUDE constraint violation (race with another requester)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Another booking just took these dates. Please pick different dates.");
        }
    }


    /**
     * Get one booking (must belong to current user).
     */
    public Booking getMine(Authentication auth, UUID bookingId) {
        User user = AuthUtils.requireUser(users, auth);
        Booking b = bookings.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!b.getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your booking");
        }
        return b;
    }

    /**
     * List current user's bookings.
     */
    public List<Booking> listMine(Authentication auth) {
        User user = AuthUtils.requireUser(users, auth);
        return bookings.findByUserId(user.getId());
    }


    public BookingDetailsDto getDetails(UUID bookingId, Authentication auth) {
        User u = AuthUtils.requireUser(users, auth);

        Booking b = bookings.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!b.getUserId().equals(u.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your booking");
        }

        Spot sp = spots.findById(b.getSpotId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Spot not found"));

        Store st = stores.findById(sp.getStoreId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));

        StoreSummary storeDto = new StoreSummary(st.getId(), st.getName(), st.getAddress(), st.getCity(), st.getZipCode());
        SpotSummary spotDto  = new SpotSummary(sp.getId(), sp.getStoreId(), sp.getPricePerDay(), sp.getAvailable());

        return new BookingDetailsDto(
                b.getId(), b.getUserId(), b.getSpotId(),
                b.getStartDate(), b.getEndDate(),
                b.getStatus(), b.getTotalPrice(),
                storeDto, spotDto
        );
    }

    // BookingService.java
    public List<Booking> listForUser(Authentication auth) {
        User u = AuthUtils.requireUser(users, auth);
        return bookings.findByUserIdOrderByStartDateDesc(u.getId());
    }

    @Transactional
    public Booking markPaid(UUID bookingId) {
        var b = bookings.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        // Idempotency: if already PAID/REFUNDED, just return
        if ("PAID".equals(b.getStatus()) || "REFUNDED".equals(b.getStatus())) {
            return b;
        }

        // Only transition PENDING -> PAID
        if ("PENDING".equals(b.getStatus())) {
            b.setStatus("PAID");
            // b.setPaidAt(Instant.now()); // if you add this column later
            b = bookings.save(b);
        }

        return b;
    }


    @Transactional
    public Booking cancelMyBooking(UUID bookingId, Authentication auth) {
        User me = AuthUtils.requireUser(users, auth);
        Booking b = bookings.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (!b.getUserId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your booking");
        }

        // idempotency
        if ("CANCELLED".equals(b.getStatus()) || "REFUNDED".equals(b.getStatus())) return b;

        switch (b.getStatus()) {
            case "PENDING" -> {
                b.setStatus("CANCELLED");
                b.setCancelReason("user_cancelled");
                return bookings.save(b);
            }
            case "PAID" -> {
                // Determine the store's cutoff
                Spot sp = spots.findById(b.getSpotId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Spot not found"));
                Store st = stores.findById(sp.getStoreId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));

                int cutoffHours = st.getCancellationCutoffHours() != null ? st.getCancellationCutoffHours() : 24;

                // Interpret "start" at midnight UTC of the booking's startDate (simple MVP rule).
                // You can enhance later using store timezone if you store it.
                ZonedDateTime startAt = b.getStartDate().atStartOfDay(ZoneOffset.UTC);
                Duration untilStart = Duration.between(ZonedDateTime.now(ZoneOffset.UTC), startAt);
                boolean beforeCutoff = untilStart.toHours() >= cutoffHours;

                if (beforeCutoff) {
                    // full refund MVP
                    if (b.getPaymentIntentId() == null || b.getPaymentIntentId().isBlank()) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "Payment not recorded for refund");
                    }
                    try {
                        RefundCreateParams params = RefundCreateParams.builder()
                                .setPaymentIntent(b.getPaymentIntentId())
                                .build(); // full refund
                        Refund r = Refund.create(params);

                        b.setRefundId(r.getId());
                        b.setRefundedAt(Instant.now());

                        if (r.getAmount() != null) b.setRefundAmountCents(BigDecimal.valueOf(r.getAmount().longValue()));
                        b.setStatus("REFUNDED");
                        b.setCancelReason("user_cancelled_before_cutoff");
                        return bookings.save(b);
                    } catch (StripeException se) {
                        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Stripe refund failed: " + se.getMessage());
                    }
                } else {
                    // after cutoff: cancel, no refund
                    b.setStatus("CANCELLED");
                    b.setCancelReason("user_cancelled_after_cutoff");
                    return bookings.save(b);
                }
            }
            default -> throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot cancel in status: " + b.getStatus());
        }
    }
}


