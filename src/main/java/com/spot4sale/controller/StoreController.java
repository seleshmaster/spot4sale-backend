
package com.spot4sale.controller;

import com.spot4sale.dto.CreateSpotRequest;
import com.spot4sale.dto.CreateStoreRequest;
import com.spot4sale.dto.StoreNearbyDTO;
import com.spot4sale.entity.Spot;
import com.spot4sale.entity.Store;
import com.spot4sale.repository.SpotRepository;
import com.spot4sale.repository.StoreRepository;
import com.spot4sale.repository.UserRepository;
import com.spot4sale.service.AuthUtils;
import com.spot4sale.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stores")
@RequiredArgsConstructor
public class StoreController {
  private final StoreRepository stores;
  private final SpotRepository spots;
  private final AuthUtils authUtils;
  private final StoreService storeService;
  private final UserRepository users;


  @PreAuthorize("hasAnyRole('USER','STORE_OWNER')")
  @PostMapping
  public Store createStore(@RequestBody @Valid CreateStoreRequest r, Authentication auth){
   // authUtils.requireRole(auth, "STORE_OWNER");
    UUID ownerId = authUtils.currentUserId(auth);
    Store s = new Store(null, ownerId, r.name(), r.description(), r.address(),
      r.city(), r.zipCode(), r.latitude(), r.longitude());
    return stores.save(s);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Store> get(@PathVariable UUID id){
    return stores.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/search")
  public List<Store> search(@RequestParam(required=false) String zip,
                            @RequestParam(required=false) String city){
    if (zip != null) return stores.findByZipCode(zip);
    if (city != null) return stores.findByCityIgnoreCase(city);
    return stores.findAll();
  }

  @PostMapping("/{storeId}/spots")
  public Spot addSpot(@PathVariable UUID storeId, @RequestBody @Valid CreateSpotRequest r){
    return spots.save(new Spot(null, storeId, r.pricePerDay(), r.available() != null ? r.available() : true));
  }

//  @GetMapping("/{storeId}/spots")
//  public List<Spot> listSpots(@PathVariable UUID storeId){ return spots.findByStoreId(storeId); }

  @GetMapping
  public Page<Store> listStores(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
    return stores.findAll(PageRequest.of(page, size, Sort.by("name")));
  }

  @GetMapping("/search/nearby")
  public List<StoreNearbyDTO> nearby(
      @RequestParam double lat,
      @RequestParam double lon,
      @RequestParam(defaultValue = "5000") double radiusMeters,
      @RequestParam(defaultValue = "20") int limit,
      @RequestParam(defaultValue = "0") int offset) {
    return storeService.searchNearby(lat, lon, radiusMeters, limit, offset);
  }

  @GetMapping("/{id}/connected-account")
  public java.util.Map<String, Object> connectedAccount(@PathVariable java.util.UUID id){
    var store = stores.findById(id).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Store not found"));
    var owner = users.findById(store.getOwnerId()).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Owner not found"));
    String acct = owner.getStripeAccountId();
    boolean enabled = owner.getChargesEnabled() != null && owner.getChargesEnabled();
    return java.util.Map.of("connectedAccountId", acct == null ? "" : acct, "chargesEnabled", enabled);
  }
}
