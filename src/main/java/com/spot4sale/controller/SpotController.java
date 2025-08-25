package com.spot4sale.controller;

import com.spot4sale.dto.CreateSpotRequest;
import com.spot4sale.dto.UpdateSpotRequest;
import com.spot4sale.entity.Spot;
import com.spot4sale.service.SpotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class SpotController {
    private final SpotService svc;
    public SpotController(SpotService svc){ this.svc = svc; }

    @GetMapping("/spots/{spotId}")
    public Spot getById(@PathVariable UUID spotId, Authentication auth) {
        return svc.getSpot(spotId, auth);
    }

    /** Owner-only: create a spot */
    @PostMapping("/spots")
    @ResponseStatus(HttpStatus.CREATED)
    public Spot create(@Valid @RequestBody CreateSpotRequest r, Authentication auth) {
        return svc.create(r, auth);
    }

    /** Owner-only: update price/availability */
    @PatchMapping("/spots/{spotId}")
    public Spot update(@PathVariable UUID spotId, @Valid @RequestBody UpdateSpotRequest r, Authentication auth) {
        return svc.update(spotId, r, auth);
    }
}
