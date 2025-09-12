// src/main/java/com/spot4sale/service/StoreService.java
package com.spot4sale.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spot4sale.dto.*;
import com.spot4sale.entity.*;
import com.spot4sale.repository.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class HostService {

    private final HostRepository hostRepository;
    private final BoothRepository boothRepository;
    private final UserRepository userRepository;
    private final AuthUtils authUtils;
    private final GeocodingService geocodingService;
    private final ObjectMapper objectMapper;

    // Repos used for availability/calendar
    private final HostBlackoutRepository blackouts;            // expect: findByStoreIdAndDateBetween(...)
    private final HostOpenSeasonRepository seasons;// expect: findByStoreId(...)
    private final AmenityRepository amenityRepository;
    private final HostTypeRepository hostTypeRepository;
    private final HostCategoryRepository hostCategoryRepository;

    /* ---------- Commands ---------- */

    @Transactional
    public Host createHost(@Valid CreateHostRequest r, Authentication auth) {
        UUID ownerId = authUtils.currentUserId(auth);

        // Build address string for geocoding
        String fullAddress = String.format("%s, %s, %s", r.address(), r.city(), r.zipCode());

        // Fetch coordinates asynchronously
        GeocodingService.LatLon latLon;
        try {
            latLon = geocodingService.fetchLatLon(fullAddress).get(); // wait for result
        } catch (Exception e) {
            latLon = new GeocodingService.LatLon(0, 0); // fallback if Google fails
        }

        // Create host entity
        Host host = new Host();
        host.setId(null);
        host.setOwnerId(ownerId);
        host.setName(r.name());
        host.setDescription(r.description());
        host.setAddress(r.address());
        host.setCity(r.city());
        host.setZipCode(r.zipCode());
        host.setLatitude(latLon.lat());
        host.setLongitude(latLon.lon());
        host.setCancellationCutoffHours(r.cancellationCutoffHours() != null ? r.cancellationCutoffHours() : 24);
        host.setImages(r.images() != null ? r.images().toArray(new String[0]) : null);
        host.setThumbnail(r.thumbnail());

        // Convert characteristics map to JSON string
        if (r.characteristics() != null) {
            try {
                host.setCharacteristics(objectMapper.writeValueAsString(r.characteristics()));
            } catch (JsonProcessingException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid characteristics JSON", e);
            }
        }

        // Host Type and Category
        host.setHostType(
                hostTypeRepository.findByName(r.hostTypeName())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "HostType not found"))
        );

        host.setHostCategory(
                hostCategoryRepository.findByName(r.hostCategoryName())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "HostCategory not found"))
        );

        // New attributes
        host.setDefaultPrice(r.defaultPrice());
        host.setMaxBooths(r.maxBooths());
        if (r.operatingHours() != null) {
            try {
                host.setOperatingHours(objectMapper.writeValueAsString(r.operatingHours()));
            } catch (JsonProcessingException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid operatingHours JSON", e);
            }
        } else {
            host.setOperatingHours("{}"); // empty JSON object
        }
        host.setContactEmail(r.contactEmail());
        host.setContactPhone(r.contactPhone());
        host.setTags(r.tags() != null ? r.tags().toArray(new String[0]) : null);
        host.setFootTrafficEstimate(r.footTrafficEstimate());
        host.setCancellationPolicy(r.cancellationPolicy());
        host.setBookingWindowDays(r.bookingWindowDays());
        host.setActive(r.active() != null ? r.active() : true);

        // Handle amenities relationship (many-to-many)
        if (r.amenityIds() != null && !r.amenityIds().isEmpty()) {
            Set<Amenity> amenities = new HashSet<>(amenityRepository.findAllById(r.amenityIds()));
            if (amenities.size() != r.amenityIds().size()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more amenities not found");
            }
            host.setAmenities(amenities);
        }

        // Timestamps
        host.setCreatedAt(Instant.from(LocalDateTime.now()));
        host.setUpdatedAt(Instant.from(LocalDateTime.now()));

        Host saved = hostRepository.save(host);

        // Promote the creator to HOST_OWNER if still USER
        User me = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!"HOST_OWNER".equalsIgnoreCase(me.getRole())) {
            me.setRole("HOST_OWNER");
            userRepository.save(me);
        }

        return saved;
    }


    @Transactional
    public Booth addSpot(UUID storeId, @Valid CreateBoothRequest r, Authentication auth) {
        // Ensure the caller owns the store
        Host host = hostRepository.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        UUID me = authUtils.currentUserId(auth);
        if (!host.getOwnerId().equals(me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your store");
        }
        Booth sp = new Booth(null, storeId, r.pricePerDay(), r.available() != null ? r.available() : true);
        return boothRepository.save(sp);
    }


    /* ---------- Queries ---------- */

    @Transactional(readOnly = true)
    public HostResponseDTO get(UUID id) {
        return hostRepository.findById(id)
                .map(HostResponseDTO::from)
                .orElse(null); // return null if store not found
    }

    @Transactional(readOnly = true)
    public List<HostSummaryDTO> search(String zip, String city) {
        return hostRepository.findStoresByCityOrZip(zip,  city);
    }

    @Transactional(readOnly = true)
    public Page<Host> list(int page, int size) {
        return hostRepository.findAll(PageRequest.of(page, size, Sort.by("name")));
    }

    @Transactional(readOnly = true)
    public List<HostSummaryDTO> searchNearby(double lat, double lon, double radiusMeters, int limit, int offset) {
        return hostRepository.searchNearby(lat, lon, radiusMeters, limit, offset);
    }

    public List<HostSummaryDTO> search(String zip, String city, Pageable pageable) {
       return hostRepository.searchByCityOrZip(zip, city);
    }


    @Transactional(readOnly = true)
    public Map<String, Object> connectedAccount(UUID storeId, Authentication auth) {
        Host store = hostRepository.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));

        // Optional: require owner to see payout info
        UUID me = authUtils.currentUserId(auth);
        if (!store.getOwnerId().equals(me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your store");
        }

        User owner = userRepository.findById(store.getOwnerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Owner not found"));

        String acct = owner.getStripeAccountId();
        boolean enabled = owner.getChargesEnabled() != null && owner.getChargesEnabled();

        return Map.of(
                "connectedAccountId", acct == null ? "" : acct,
                "chargesEnabled", enabled
        );
    }

    @Transactional(readOnly = true)
    public List<Booth> listSpots(UUID storeId) {
        return boothRepository.findByStoreId(storeId);
    }

    /**
     * Simple range availability summary:
     *  - blackoutDays: all days blocked by explicit blackouts in [from, to]
     *  - openDays (legacy): left empty here; seasons should drive UI detail.
     */
    // StoreService.java (replace the whole getAvailability method)
    @Transactional(readOnly = true)
    public AvailabilityRangeDTO getAvailability(UUID storeId, LocalDate from, LocalDate to) {
        // blackouts in range
        var bs = blackouts.findByHostIdAndDateBetween(storeId, from, to);
        var blackoutDays = bs.stream().map(HostBlackout::getDate).toList();

        // seasons that overlap the requested range
        // (assumes repo has findByStoreId; we filter overlaps in memory)
        var allSeasons = seasons.findByStoreId(storeId);
        var overlappingSeasons = allSeasons.stream()
                .filter(s -> !(s.getEndDate().isBefore(from) || s.getStartDate().isAfter(to)))
                .map(s -> new SeasonDTO(
                        s.getId(),
                        s.getStartDate(),
                        s.getEndDate(),
                        s.getOpenWeekdays(),   // List<Integer> (from CSV converter in entity)
                        s.getNote()
                ))
                .toList();

        // "openWeekdays" is legacy here; keep empty for now (seasons govern real rules)
        return new AvailabilityRangeDTO(blackoutDays, List.of(), overlappingSeasons);
    }


    @Transactional
    public void setBlackouts(UUID hostId, List<LocalDate> days, Authentication auth, AuthUtils authUtils) {
        // owner check
        var userId = authUtils.currentUserId(auth);
        var host = hostRepository.findById(hostId).orElseThrow();
        if (!host.getOwnerId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        // upsert MVP: clear existing in that span, then insert all provided
        if (!days.isEmpty()) {
            var min = days.stream().min(LocalDate::compareTo).orElse(LocalDate.now());
            var max = days.stream().max(LocalDate::compareTo).orElse(LocalDate.now());
            var existing = blackouts.findByHostIdAndDateBetween(hostId, min, max);
            blackouts.deleteAll(existing);
        }

        for (LocalDate d : days) {
            HostBlackout b = new HostBlackout();
            b.setHostId(hostId);        // pass Host entity
            b.setDate(d);
            b.setReason("Owner-set");
            blackouts.save(b);
        }
    }

    /** Core check used by BookingService: date range must be covered by seasons AND not in blackouts. */
    @Transactional(readOnly = true)
    public boolean isOpenAccordingToSeasons(UUID storeId, LocalDate start, LocalDate end) {
        // If no seasons defined, treat as open (only blackouts apply)
        var storeSeasons = seasons.findByStoreId(storeId);
        if (storeSeasons.isEmpty()) {
            return blackouts.findByHostIdAndDateBetween(storeId, start, end).isEmpty();
        }

        // Gather blackouts in range for quick lookup
        var blackoutDays = new HashSet<>(
                blackouts.findByHostIdAndDateBetween(storeId, start, end).stream()
                        .map(HostBlackout::getDate).toList()
        );

        for (var d = start; !d.isAfter(end); d = d.plusDays(1)) {
            if (blackoutDays.contains(d)) return false;

            boolean covered = false;
            int isoDow = d.getDayOfWeek().getValue(); // 1..7

            for (var s : storeSeasons) {
                // date must be within the season window
                if (d.isBefore(s.getStartDate()) || d.isAfter(s.getEndDate())) continue;

                // weekday must be allowed if a whitelist is provided
                var ow = s.getOpenWeekdays(); // List<Integer> (empty => all weekdays allowed)
                if (ow != null && !ow.isEmpty() && !ow.contains(isoDow)) continue;

                covered = true;
                break;
            }
            if (!covered) return false; // this day not allowed by any season
        }
        return true;
    }

    @Transactional(readOnly = true)
    public List<SeasonDTO> listSeasons(UUID storeId) {
        return seasons.findByStoreId(storeId).stream()
                .map(s -> new SeasonDTO(
                        s.getId(),
                        s.getStartDate(),
                        s.getEndDate(),
                        s.getOpenWeekdays(),   // List<Integer>
                        s.getNote()
                ))
                .toList();
    }

    @Transactional
    public SeasonDTO addSeason(UUID storeId,
                               LocalDate start,
                               LocalDate end,
                               List<Integer> openWeekdays,
                               String note,
                               Authentication auth) {
        var userId = authUtils.currentUserId(auth);
        var store = hostRepository.findById(storeId).orElseThrow();
        if (!store.getOwnerId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        var s = new HostOpenSeason();
        s.setStoreId(storeId);
        s.setStartDate(start);
        s.setEndDate(end);
        s.setOpenWeekdays(openWeekdays);  // <-- setter writes CSV internally
        s.setNote(note);

        s = seasons.save(s);
        return new SeasonDTO(s.getId(), s.getStartDate(), s.getEndDate(), s.getOpenWeekdays(), s.getNote());
    }

    @Transactional
    public void deleteSeason(UUID storeId, UUID seasonId, Authentication auth) {
        var userId = authUtils.currentUserId(auth);
        var store = hostRepository.findById(storeId).orElseThrow();
        if (!store.getOwnerId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        var s = seasons.findById(seasonId).orElseThrow();
        if (!s.getStoreId().equals(storeId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        seasons.delete(s);
    }

    @Transactional
    public Host updateStore(UUID storeId, @Valid CreateHostRequest r, Authentication auth) {
        UUID userId = authUtils.currentUserId(auth);
        Host store = hostRepository.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));

        if (!store.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your store");
        }

        // Update fields
        store.setName(r.name());
        store.setDescription(r.description());
        store.setAddress(r.address());
        store.setCity(r.city());
        store.setZipCode(r.zipCode());
        store.setLatitude(r.latitude() != null ? r.latitude() : store.getLatitude());
        store.setLongitude(r.longitude() != null ? r.longitude() : store.getLongitude());
        store.setCancellationCutoffHours(r.cancellationCutoffHours() != null ? r.cancellationCutoffHours() : store.getCancellationCutoffHours());
        store.setImages( r.images() != null ? r.images().toArray(new String[0]) : null);

        return hostRepository.save(store);
    }

    @Transactional
    public void deleteStore(UUID storeId, Authentication auth) {
        UUID userId = authUtils.currentUserId(auth);
        Host store = hostRepository.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));

        if (!store.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your store");
        }

        // Optional: delete spots associated with this store
        var storeSpots = boothRepository.findByStoreId(storeId);
        boothRepository.deleteAll(storeSpots);

        // delete seasons and blackouts
        seasons.findByStoreId(storeId).forEach(seasons::delete);
        blackouts.findByHostId(storeId)
                .forEach(blackouts::delete);

        hostRepository.delete(store);
    }

}
