package com.spot4sale.controller;

import com.spot4sale.dto.CreateSpotRequest;
import com.spot4sale.dto.CreateStoreRequest;
import com.spot4sale.dto.StoreNearbyDTO;
import com.spot4sale.entity.Spot;
import com.spot4sale.entity.Store;
import com.spot4sale.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

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
}
