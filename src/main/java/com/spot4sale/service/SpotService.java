package com.spot4sale.service;

import com.spot4sale.dto.CreateSpotRequest;
import com.spot4sale.dto.UpdateSpotRequest;
import com.spot4sale.entity.Spot;
import com.spot4sale.entity.Store;
import com.spot4sale.entity.User;
import com.spot4sale.repository.SpotRepository;
import com.spot4sale.repository.StoreOpenSeasonRepository;
import com.spot4sale.repository.StoreRepository;
import com.spot4sale.repository.UserRepository;
import com.spot4sale.service.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SpotService {
    private final SpotRepository spots;
    private final StoreRepository stores;
    private final UserRepository users;


    public List<Spot> listByStore(UUID storeId) {
        Store store = stores.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        return spots.findByStoreId(store.getId());
    }

    public Spot create(CreateSpotRequest r, Authentication auth) {
        User owner = AuthUtils.requireUser(users, auth);
        Store store = stores.findById(r.storeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        if (!store.getOwnerId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your store");
        }
        Spot s = new Spot();
        s.setStoreId(store.getId());
        s.setPricePerDay(r.pricePerDay());
        s.setAvailable(Boolean.TRUE.equals(r.available()));
        return spots.save(s);
    }

    public Spot update(UUID spotId, UpdateSpotRequest r, Authentication auth) {
        User owner = AuthUtils.requireUser(users, auth);
        Spot s = spots.findById(spotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Spot not found"));
        Store store = stores.findById(s.getStoreId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        if (!store.getOwnerId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your store");
        }
        if (r.pricePerDay() != null) s.setPricePerDay(r.pricePerDay());
        if (r.available()   != null) s.setAvailable(r.available());
        return spots.save(s);
    }

    public void delete(UUID spotId, Authentication auth) {
        User owner = AuthUtils.requireUser(users, auth);
        Spot s = spots.findById(spotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Spot not found"));
        Store store = stores.findById(s.getStoreId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        if (!store.getOwnerId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your store");
        }
        spots.deleteById(spotId);
    }

    public Spot getSpot(UUID spotId, Authentication auth) {
        User owner = AuthUtils.requireUser(users, auth);
        Spot s = spots.findById(spotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Spot not found"));
        return s;
    }
}
