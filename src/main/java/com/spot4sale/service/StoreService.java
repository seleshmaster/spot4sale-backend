// src/main/java/com/spot4sale/service/StoreService.java
package com.spot4sale.service;

import com.spot4sale.dto.CreateSpotRequest;
import com.spot4sale.dto.CreateStoreRequest;
import com.spot4sale.dto.StoreNearbyDTO;
import com.spot4sale.entity.Spot;
import com.spot4sale.entity.Store;
import com.spot4sale.entity.User;
import com.spot4sale.repository.SpotRepository;
import com.spot4sale.repository.StoreRepository;
import com.spot4sale.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class StoreService {

    private final StoreRepository stores;
    private final SpotRepository spots;
    private final UserRepository users;
    private final AuthUtils authUtils;

    public StoreService(StoreRepository stores,
                        SpotRepository spots,
                        UserRepository users,
                        AuthUtils authUtils) {
        this.stores = stores;
        this.spots = spots;
        this.users = users;
        this.authUtils = authUtils;
    }

    /* ---------- Commands ---------- */

    @Transactional
    public Store createStore(@Valid CreateStoreRequest r, Authentication auth) {
        UUID ownerId = authUtils.currentUserId(auth);

        Store s = new Store(
                null, ownerId, r.name(), r.description(), r.address(),
                r.city(), r.zipCode(), r.latitude(), r.longitude(),
                r.cancellationCutoffHours() != null ? r.cancellationCutoffHours() : 24);

        Store saved = stores.save(s);

        // Promote the creator to STORE_OWNER if they are still USER
        User me = users.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!"STORE_OWNER".equals(me.getRole())) {
            me.setRole("STORE_OWNER");
            users.save(me);
        }
        return saved;
    }

    @Transactional
    public Spot addSpot(UUID storeId, @Valid CreateSpotRequest r, Authentication auth) {
        // Ensure the caller owns the store
        Store st = stores.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        UUID me = authUtils.currentUserId(auth);
        if (!st.getOwnerId().equals(me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your store");
        }
        Spot sp = new Spot(null, storeId, r.pricePerDay(), r.available() != null ? r.available() : true);
        return spots.save(sp);
    }

    /* ---------- Queries ---------- */

    @Transactional(readOnly = true)
    public Optional<Store> get(UUID id) {
        return stores.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Store> search(String zip, String city) {
        if (zip != null) return stores.findByZipCode(zip);
        if (city != null) return stores.findByCityIgnoreCase(city);
        return stores.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Store> list(int page, int size) {
        return stores.findAll(PageRequest.of(page, size, Sort.by("name")));
    }

    @Transactional(readOnly = true)
    public List<StoreNearbyDTO> searchNearby(double lat, double lon, double radiusMeters, int limit, int offset) {
        return stores.searchNearby(lat, lon, radiusMeters, limit, offset);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> connectedAccount(UUID storeId, Authentication auth) {
        Store store = stores.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));

        // Optional: require owner to see payout info
        UUID me = authUtils.currentUserId(auth);
        if (!store.getOwnerId().equals(me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your store");
        }

        User owner = users.findById(store.getOwnerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found"));

        String acct = owner.getStripeAccountId();
        boolean enabled = owner.getChargesEnabled() != null && owner.getChargesEnabled();

        return Map.of(
                "connectedAccountId", acct == null ? "" : acct,
                "chargesEnabled", enabled
        );
    }

    @Transactional(readOnly = true)
    public List<Spot> listSpots(UUID storeId) {
        return spots.findByStoreId(storeId);
    }
}
