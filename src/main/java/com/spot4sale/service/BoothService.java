package com.spot4sale.service;

import com.spot4sale.dto.CreateBoothRequest;
import com.spot4sale.dto.UpdateHostRequest;
import com.spot4sale.entity.Booth;
import com.spot4sale.entity.Host;
import com.spot4sale.entity.User;
import com.spot4sale.repository.BoothRepository;
import com.spot4sale.repository.HostRepository;
import com.spot4sale.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoothService {
    private final BoothRepository boothRepository;
    private final HostRepository hostRepository;
    private final UserRepository userRepository;


    public List<Booth> listByStore(UUID storeId) {
        Host store = hostRepository.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        return boothRepository.findByStoreId(store.getId());
    }

    public Booth create(CreateBoothRequest r, Authentication auth) {
        User owner = AuthUtils.requireUser(userRepository, auth);
        Host host = hostRepository.findById(r.storeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        if (!host.getOwnerId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your host");
        }
        Booth s = new Booth();
        s.setStoreId(host.getId());
        s.setPricePerDay(r.pricePerDay());
        s.setAvailable(Boolean.TRUE.equals(r.available()));
        return boothRepository.save(s);
    }

    public Booth update(UUID spotId, UpdateHostRequest r, Authentication auth) {
        User owner = AuthUtils.requireUser(userRepository, auth);
        Booth s = boothRepository.findById(spotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Spot not found"));
        Host store = hostRepository.findById(s.getStoreId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        if (!store.getOwnerId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your store");
        }
        if (r.pricePerDay() != null) s.setPricePerDay(r.pricePerDay());
        if (r.available()   != null) s.setAvailable(r.available());
        return boothRepository.save(s);
    }

    public void delete(UUID spotId, Authentication auth) {
        User owner = AuthUtils.requireUser(userRepository, auth);
        Booth s = boothRepository.findById(spotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Spot not found"));
        Host store = hostRepository.findById(s.getStoreId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        if (!store.getOwnerId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your store");
        }
        boothRepository.deleteById(spotId);
    }

    public Booth getSpot(UUID spotId, Authentication auth) {
        User owner = AuthUtils.requireUser(userRepository, auth);
        Booth booth = boothRepository.findById(spotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Spot not found"));
        return booth;
    }
}
