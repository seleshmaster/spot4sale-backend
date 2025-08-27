package com.spot4sale.controller;

import com.spot4sale.dto.*;
import com.spot4sale.entity.Spot;
import com.spot4sale.entity.Store;
import com.spot4sale.service.AuthUtils;
import com.spot4sale.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.support.AopUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final AuthUtils authUtils;

    @PreAuthorize("hasAnyRole('USER','STORE_OWNER')")
    @PostMapping
    public Store createStore(@RequestBody @Valid CreateStoreRequest r, Authentication auth) {
        return storeService.createStore(r, auth);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Store> get(@PathVariable UUID id) {
        return storeService.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Store> search(@RequestParam(required = false) String zip,
                              @RequestParam(required = false) String city) {
        return storeService.search(zip, city);
    }

    @PostMapping("/{storeId}/spots")
    public Spot addSpot(@PathVariable UUID storeId,
                        @RequestBody @Valid CreateSpotRequest r,
                        Authentication auth) {
        return storeService.addSpot(storeId, r, auth);
    }

    @GetMapping("/{storeId}/spots")
    public List<Spot> listSpots(@PathVariable UUID storeId) {
        return storeService.listSpots(storeId);
    }

    @GetMapping
    public Page<Store> listStores(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size) {
        return storeService.list(page, size);
    }

    @GetMapping("/search/nearby")
    public List<StoreNearbyDTO> nearby(@RequestParam double lat,
                                       @RequestParam double lon,
                                       @RequestParam(defaultValue = "5000") double radiusMeters,
                                       @RequestParam(defaultValue = "20") int limit,
                                       @RequestParam(defaultValue = "0") int offset) {
        return storeService.searchNearby(lat, lon, radiusMeters, limit, offset);
    }

    @GetMapping("/{id}/connected-account")
    public Map<String, Object> connectedAccount(@PathVariable UUID id, Authentication auth) {
        return storeService.connectedAccount(id, auth);
    }

    // GET availability for a calendar (owner & customers)
    @GetMapping("/{storeId}/availability")
    public AvailabilityRangeDTO availability(@PathVariable UUID storeId,
                                             @RequestParam LocalDate from,
                                             @RequestParam LocalDate to) {
        return storeService.getAvailability(storeId, from, to);
    }

    // OWNER: set blackout dates (simple MVP)
    @PreAuthorize("hasAnyRole('STORE_OWNER')")
    @PostMapping("/{storeId}/availability/blackouts")
    public void setBlackouts(@PathVariable UUID storeId,
                             @RequestBody List<LocalDate> days,
                             Authentication auth) {
        storeService.setBlackouts(storeId, days, auth, authUtils);
    }

    @PreAuthorize("hasAnyRole('STORE_OWNER')")
    @GetMapping("/{storeId}/availability/seasons")
    public List<SeasonDTO> listSeasons(@PathVariable UUID storeId) {
        return storeService.listSeasons(storeId);
    }

    @PreAuthorize("hasAnyRole('STORE_OWNER')")
    @PostMapping("/{storeId}/availability/seasons")
    public SeasonDTO addSeason(@PathVariable UUID storeId, @RequestBody @Valid CreateSeasonRequest r, Authentication auth) {
        return storeService.addSeason(storeId, r.startDate(), r.endDate(), Arrays.stream(r.openWeekdays()) // Create an IntStream from the int array
                .boxed()
                .collect(Collectors.toList()), r.note(), auth);
    }

    @PreAuthorize("hasAnyRole('STORE_OWNER')")
    @DeleteMapping("/{storeId}/availability/seasons/{seasonId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSeason(@PathVariable UUID storeId, @PathVariable UUID seasonId, Authentication auth) {
        storeService.deleteSeason(storeId, seasonId, auth);
    }

    @PutMapping("/{storeId}")
    public Store updateStore(@PathVariable UUID storeId,
                             @Valid @RequestBody CreateStoreRequest request,
                             Authentication auth) {
        return storeService.updateStore(storeId, request, auth);
    }

    @DeleteMapping("/{storeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStore(@PathVariable UUID storeId, Authentication auth) {
        storeService.deleteStore(storeId, auth);
    }


}
