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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookings;
    private final SpotRepository spots;
    private final StoreRepository stores;
    private final UserRepository users;

    public BookingService(BookingRepository bookings, SpotRepository spots, StoreRepository stores, UserRepository users) {
        this.bookings = bookings;
        this.spots = spots;
        this.stores = stores;
        this.users = users;
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
        b.setStatus("CONFIRMED"); // or "PENDING" if you want payment-first
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

}

