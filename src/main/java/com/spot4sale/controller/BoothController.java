package com.spot4sale.controller;

import com.spot4sale.dto.CreateBoothRequest;
import com.spot4sale.dto.UpdateHostRequest;
import com.spot4sale.entity.Booth;
import com.spot4sale.service.BoothService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class BoothController {
    private final BoothService svc;
    public BoothController(BoothService svc){ this.svc = svc; }

    @GetMapping("/spots/{spotId}")
    public Booth getById(@PathVariable UUID spotId, Authentication auth) {
        return svc.getSpot(spotId, auth);
    }

    /** Owner-only: create a spot */
    @PostMapping("/spots")
    @ResponseStatus(HttpStatus.CREATED)
    public Booth create(@Valid @RequestBody CreateBoothRequest r, Authentication auth) {
        return svc.create(r, auth);
    }

    /** Owner-only: update price/availability */
    @PatchMapping("/spots/{spotId}")
    public Booth update(@PathVariable UUID spotId, @Valid @RequestBody UpdateHostRequest r, Authentication auth) {
        return svc.update(spotId, r, auth);
    }
}
