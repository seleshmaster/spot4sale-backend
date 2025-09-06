// src/main/java/com/spot4sale/service/OwnerService.java
package com.spot4sale.service;

import com.spot4sale.entity.Booking;
import com.spot4sale.entity.Booth;
import com.spot4sale.entity.Host;
import com.spot4sale.repository.BookingRepository;
import com.spot4sale.repository.BoothRepository;
import com.spot4sale.repository.HostRepository;
import com.spot4sale.repository.UserRepository;
import com.spot4sale.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class OwnerService {
    private final HostRepository stores;
    private final BoothRepository spots;
    private final BookingRepository bookings;
    private final UserRepository users;

    public OwnerService(HostRepository stores, BoothRepository spots, BookingRepository bookings, UserRepository users) {
        this.stores = stores; this.spots = spots; this.bookings = bookings; this.users = users;
    }

    public List<Host> myStores(Authentication auth){
        User me = AuthUtils.requireUser(users, auth);
        return stores.findByOwnerId(me.getId());
    }

    public List<Booth> spotsForStore(UUID storeId, Authentication auth){
        ensureOwner(storeId, auth);
        return spots.findByStoreId(storeId);
    }

    public List<Booking> bookingsForStore(UUID storeId, Authentication auth){
        ensureOwner(storeId, auth);
        // gather all spotIds for the store, then bookings for those
        var spotIds = spots.findByStoreId(storeId).stream().map(Booth::getId).toList();
        if (spotIds.isEmpty()) return List.of();
        return bookings.findBySpotIdInOrderByStartDateDesc(spotIds);
    }

    public Booking updateBookingStatus(UUID storeId, UUID bookingId, String newStatus, Authentication auth){
        ensureOwner(storeId, auth);
        var b = bookings.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        // ensure the booking belongs to this store
        var sp = spots.findById(b.getSpotId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Spot not found"));
        if (!sp.getStoreId().equals(storeId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot modify booking outside your store");
        }
        b.setStatus(newStatus); // e.g., CONFIRMED, CANCELLED, COMPLETED
        return bookings.save(b);
    }

    private void ensureOwner(UUID storeId, Authentication auth){
        User me = AuthUtils.requireUser(users, auth);
        var st = stores.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        if (!st.getOwnerId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your store");
        }
    }
}
