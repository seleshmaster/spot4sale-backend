
package com.spot4sale.repository;

import com.spot4sale.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UserRepository extends JpaRepository<User, java.util.UUID> {
  Optional<User> findByEmail(String email);
}
