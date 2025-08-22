
package com.spot4sale.service;

import com.spot4sale.dto.StoreNearbyDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StoreService {

  @PersistenceContext
  private EntityManager em;

  @Transactional
  public List<StoreNearbyDTO> searchNearby(double lat, double lon, double radiusMeters, int limit, int offset) {
    String sql = """
      select id, name, address, city, zip_code, latitude, longitude,
             ST_Distance(location, ST_SetSRID(ST_MakePoint(:lon,:lat),4326)::geography) as dist
      from store
      where location is not null
        and ST_DWithin(location, ST_SetSRID(ST_MakePoint(:lon,:lat),4326)::geography, :radius)
      order by dist
      limit :limit offset :offset
      """;
    @SuppressWarnings("unchecked")
    List<Object[]> rows = em.createNativeQuery(sql)
        .setParameter("lat", lat)
        .setParameter("lon", lon)
        .setParameter("radius", radiusMeters)
        .setParameter("limit", limit)
        .setParameter("offset", offset)
        .getResultList();
    return rows.stream().map(r -> new StoreNearbyDTO(
        (UUID) r[0],
        (String) r[1],
        (String) r[2],
        (String) r[3],
        (String) r[4],
        r[5] != null ? ((Number) r[5]).doubleValue() : null,
        r[6] != null ? ((Number) r[6]).doubleValue() : null,
        ((Number) r[7]).doubleValue()
    )).toList();
  }
}
