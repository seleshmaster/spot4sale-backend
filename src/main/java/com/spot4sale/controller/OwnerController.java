// src/main/java/com/spot4sale/controller/OwnerController.java
package com.spot4sale.controller;

import com.spot4sale.entity.Booking;
import com.spot4sale.entity.Booth;
import com.spot4sale.entity.Host;
import com.spot4sale.service.OwnerService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {
    private final OwnerService owner;

    public OwnerController(OwnerService owner) { this.owner = owner; }

    @GetMapping("/stores")
    public List<Host> myStores(Authentication auth){
        return owner.myStores(auth);
    }

    @GetMapping("/stores/{storeId}/spots")
    public List<Booth> spots(@PathVariable UUID storeId, Authentication auth){
        return owner.spotsForStore(storeId, auth);
    }

    @GetMapping("/stores/{storeId}/bookings")
    public List<Booking> bookings(@PathVariable UUID storeId, Authentication auth){
        return owner.bookingsForStore(storeId, auth);
    }

    @PatchMapping("/stores/{storeId}/bookings/{bookingId}/status")
    public Booking updateStatus(@PathVariable UUID storeId,
                                @PathVariable UUID bookingId,
                                @RequestParam String status,
                                Authentication auth) {
        return owner.updateBookingStatus(storeId, bookingId, status, auth);
    }
}
