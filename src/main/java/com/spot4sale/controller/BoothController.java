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

    @GetMapping("/booths/{boothId}")
    public Booth getById(@PathVariable UUID boothId, Authentication auth) {
        return svc.getSpot(boothId, auth);
    }

    /** Owner-only: create a spot */
    @PostMapping("/booths")
    @ResponseStatus(HttpStatus.CREATED)
    public Booth create(@Valid @RequestBody CreateBoothRequest r, Authentication auth) {
        return svc.create(r, auth);
    }

    /** Owner-only: update price/availability */
    @PatchMapping("/booths/{boothId}")
    public Booth update(@PathVariable UUID boothId, @Valid @RequestBody UpdateHostRequest r, Authentication auth) {
        return svc.update(boothId, r, auth);
    }
}
