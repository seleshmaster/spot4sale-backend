package com.spot4sale.controller;

import com.spot4sale.dto.BookingDetailsDto;
import com.spot4sale.entity.Booking;
import com.spot4sale.service.AuthUtils;
import com.spot4sale.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    public static record CreateBookingRequest(UUID spotId, LocalDate startDate, LocalDate endDate) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking create(@Valid @RequestBody CreateBookingRequest r, Authentication auth) {
        return this.service.create(r.spotId(), r.startDate(), r.endDate(), auth);
    }

    @GetMapping("/{id}")
    public Booking get(@PathVariable UUID id, Authentication auth) {
        return this.service.getMine(auth, id);
    }

    @GetMapping("/me")
    public List<Booking> mine(Authentication auth) {
        return this.service.listMine(auth);
    }

    @GetMapping("/{id}/details")
    public BookingDetailsDto details(@PathVariable UUID id, Authentication auth) {
        return this.service.getDetails(id, auth);
    }

}
