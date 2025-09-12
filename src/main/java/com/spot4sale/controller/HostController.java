package com.spot4sale.controller;

import com.spot4sale.dto.*;
import com.spot4sale.entity.Booth;
import com.spot4sale.entity.Host;
import com.spot4sale.service.AuthUtils;
import com.spot4sale.service.HostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hosts")
@RequiredArgsConstructor
public class HostController {

    private final HostService storeService;
    private final AuthUtils authUtils;

    @PreAuthorize("hasAnyRole('USER','STORE_OWNER')")
    @PostMapping
    public Host createStore(@RequestBody @Valid CreateHostRequest r, Authentication auth) {
        return storeService.createHost(r, auth);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HostResponseDTO> getStore(@PathVariable UUID id) {
        HostResponseDTO dto = storeService.get(id);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{hostId}/booths")
    public Booth addSpot(@PathVariable UUID hostId,
                         @RequestBody @Valid CreateBoothRequest r,
                         Authentication auth) {
        return storeService.addSpot(hostId, r, auth);
    }

    @GetMapping("/{hostId}/booths")
    public List<Booth> listSpots(@PathVariable UUID hostId) {
        return storeService.listSpots(hostId);
    }

    @GetMapping
    public Page<Host> listStores(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        return storeService.list(page, size);
    }



//    @GetMapping("/search")
//    public List<StoreSummaryDTO> search(@RequestParam(required = false) String zip,
//                                        @RequestParam(required = false) String city) {
//        return storeService.search(zip, city);
//    }

    @GetMapping("/search/nearby")
    public List<HostSummaryDTO> nearby(@RequestParam double lat,
                                       @RequestParam double lon,
                                       @RequestParam(defaultValue = "5000") double radiusMeters,
                                       @RequestParam(defaultValue = "20") int limit,
                                       @RequestParam(defaultValue = "0") int offset) {
        return storeService.searchNearby(lat, lon, radiusMeters, limit, offset);

    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<HostSummaryDTO> search(
            @RequestParam(required = false) String zip,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return storeService.search(zip, city, PageRequest.of(page, size));
    }



    @GetMapping("/{id}/connected-account")
    public Map<String, Object> connectedAccount(@PathVariable UUID id, Authentication auth) {
        return storeService.connectedAccount(id, auth);
    }

    // GET availability for a calendar (owner & customers)
    @GetMapping("/{hostId}/availability")
    public AvailabilityRangeDTO availability(@PathVariable UUID hostId,
                                             @RequestParam LocalDate from,
                                             @RequestParam LocalDate to) {
        return storeService.getAvailability(hostId, from, to);
    }

    // OWNER: set blackout dates (simple MVP)
    @PreAuthorize("hasAnyRole('STORE_OWNER')")
    @PostMapping("/{hostId}/availability/blackouts")
    public void setBlackouts(@PathVariable UUID hostId,
                             @RequestBody List<LocalDate> days,
                             Authentication auth) {
        storeService.setBlackouts(hostId, days, auth, authUtils);
    }

    @PreAuthorize("hasAnyRole('STORE_OWNER')")
    @GetMapping("/{hostId}/availability/seasons")
    public List<SeasonDTO> listSeasons(@PathVariable UUID hostId) {
        return storeService.listSeasons(hostId);
    }

    @PreAuthorize("hasAnyRole('STORE_OWNER')")
    @PostMapping("/{hostId}/availability/seasons")
    public SeasonDTO addSeason(@PathVariable UUID hostId, @RequestBody @Valid CreateSeasonRequest r, Authentication auth) {
        return storeService.addSeason(
                hostId,
                r.startDate(),
                r.endDate(),
                r.openWeekdays() != null
                        ? Arrays.stream(r.openWeekdays())  // Convert int[] to IntStream
                        .boxed()                   // Box int to Integer
                        .collect(Collectors.toList())
                        : Collections.emptyList(),        // If null, pass empty list
                r.note(),
                auth
        );
    }

    @PreAuthorize("hasAnyRole('STORE_OWNER')")
    @DeleteMapping("/{hostId}/availability/seasons/{seasonId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSeason(@PathVariable UUID hostId, @PathVariable UUID seasonId, Authentication auth) {
        storeService.deleteSeason(hostId, seasonId, auth);
    }

    @PutMapping("/{hostId}")
    public Host updateStore(@PathVariable UUID hostId,
                            @Valid @RequestBody CreateHostRequest request,
                            Authentication auth) {
        return storeService.updateStore(hostId, request, auth);
    }

    @DeleteMapping("/{storeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStore(@PathVariable UUID storeId, Authentication auth) {
        storeService.deleteStore(storeId, auth);
    }


}
