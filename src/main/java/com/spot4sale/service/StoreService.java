// src/main/java/com/spot4sale/service/StoreService.java
package com.spot4sale.service;

import com.spot4sale.dto.*;
import com.spot4sale.entity.*;
import com.spot4sale.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository stores;
    private final SpotRepository spots;
    private final UserRepository users;
    private final AuthUtils authUtils;
    private final StoreBlackoutRepository blackouts; // you already have this
    private final StoreOpenSeasonRepository seasons;
    private final StoreWeeklyOpenRepository weeklyOpen;


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


    @Transactional(readOnly = true)
    public AvailabilityRangeDTO getAvailability(UUID storeId, LocalDate from, LocalDate to) {
        var bs = blackouts.findByStoreIdAndDayBetween(storeId, from, to);
        var blackoutDays = bs.stream().map(b -> b.getDay()).toList();

        List<Integer> openDays = weeklyOpen != null
                ? weeklyOpen.findByStoreId(storeId).stream()
                .filter(StoreWeeklyOpen::isOpen).map(StoreWeeklyOpen::getDayOfWeek).toList()
                : List.of(); // empty => treat as open all days except blackout

        return new AvailabilityRangeDTO(blackoutDays, openDays);
    }

    @Transactional
    public void setBlackouts(UUID storeId, List<LocalDate> days, Authentication auth, AuthUtils authUtils) {
        // owner check
        var userId = authUtils.currentUserId(auth);
        var store = stores.findById(storeId).orElseThrow();
        if (!store.getOwnerId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        // upsert simple: clear + insert (MVP)
        var existing = blackouts.findByStoreIdAndDayBetween(storeId,
                days.stream().min(LocalDate::compareTo).orElse(LocalDate.now()),
                days.stream().max(LocalDate::compareTo).orElse(LocalDate.now()));
        blackouts.deleteAll(existing);

        for (var d : days) {
            var b = new StoreBlackout(null, storeId, d, "Owner-set", null);
            blackouts.save(b);
        }
    }

    /** Utility used by BookingService to reject unavailable dates */
    @Transactional(readOnly = true)
    public boolean isStoreOpenForRange(UUID storeId, LocalDate start, LocalDate end) {
        // any blackout in range?
        if (!blackouts.findByStoreIdAndDayBetween(storeId, start, end).isEmpty()) return false;

        // optional weekly rule: if weekly table has rows, treat NOT listed days as closed
        var weekly = weeklyOpen.findByStoreId(storeId);
        if (!weekly.isEmpty()) {
            var openSet = weekly.stream().filter(StoreWeeklyOpen::isOpen)
                    .map(StoreWeeklyOpen::getDayOfWeek).collect(java.util.stream.Collectors.toSet());
            for (var d = start; !d.isAfter(end); d = d.plusDays(1)) {
                int dow = d.getDayOfWeek().getValue(); // 1..7
                if (!openSet.contains(dow)) return false;
            }
        }
        return true;
    }

    @Transactional(readOnly = true)
    public List<SeasonDTO> listSeasons(UUID storeId) {
        return seasons.findByStoreId(storeId).stream()
                .map(s -> new SeasonDTO(s.getId(), s.getStartDate(), s.getEndDate(), s.getOpenWeekdays(), s.getNote()))
                .toList();
    }

    @Transactional
    public SeasonDTO addSeason(UUID storeId, LocalDate start, LocalDate end, int[] openWeekdays, String note, Authentication auth) {
        var userId = authUtils.currentUserId(auth);
        var store = stores.findById(storeId).orElseThrow();
        if (!store.getOwnerId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        var s = new StoreOpenSeason();
        s.setStoreId(storeId);
        s.setStartDate(start);
        s.setEndDate(end);
        s.setOpenWeekdays(openWeekdays);
        s.setNote(note);
        s = seasons.save(s);
        return new SeasonDTO(s.getId(), s.getStartDate(), s.getEndDate(), s.getOpenWeekdays(), s.getNote());
    }

    @Transactional
    public void deleteSeason(UUID storeId, UUID seasonId, Authentication auth) {
        var userId = authUtils.currentUserId(auth);
        var store = stores.findById(storeId).orElseThrow();
        if (!store.getOwnerId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        var s = seasons.findById(seasonId).orElseThrow();
        if (!s.getStoreId().equals(storeId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        seasons.delete(s);
    }

    /** Core check used by BookingService: date range must be inside at least one season AND not in blackouts. */
    @Transactional(readOnly = true)
    public boolean isOpenAccordingToSeasons(UUID storeId, LocalDate start, LocalDate end) {
        // If no seasons defined, treat as open (only blackouts apply)
        var storeSeasons = seasons.findByStoreId(storeId);
        if (storeSeasons.isEmpty()) {
            return blackouts.findByStoreIdAndDayBetween(storeId, start, end).isEmpty();
        }

        // Each day must be covered by at least one season && weekday allowed && not blacked out
        var blackoutDays = new java.util.HashSet<LocalDate>(
                blackouts.findByStoreIdAndDayBetween(storeId, start, end).stream().map(b -> b.getDay()).toList()
        );

        for (var d = start; !d.isAfter(end); d = d.plusDays(1)) {
            if (blackoutDays.contains(d)) return false;

            boolean covered = false;
            int isoDow = d.getDayOfWeek().getValue(); // 1..7
            for (var s : storeSeasons) {
                if ((d.isAfter(s.getEndDate()) || d.isBefore(s.getStartDate()))) continue;
                int[] ow = s.getOpenWeekdays();
                if (ow != null && ow.length > 0) {
                    // must be in the allowed weekdays
                    boolean allowedDow = java.util.Arrays.stream(ow).anyMatch(v -> v == isoDow);
                    if (!allowedDow) continue;
                }
                covered = true;
                break;
            }
            if (!covered) return false; // this day not allowed by any season
        }
        return true;
    }
}


