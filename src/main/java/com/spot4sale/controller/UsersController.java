
package com.spot4sale.controller;

import com.spot4sale.entity.User;
import com.spot4sale.repository.UserRepository;
import com.spot4sale.service.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {
  private final UserRepository users;
  private final AuthUtils authUtils;

  @PutMapping("/me")
  public ResponseEntity<?> updateMe(@RequestBody Map<String,Object> patch, Authentication auth){
    User me = authUtils.currentUser(auth);
    if (patch.containsKey("name")) me.setName((String) patch.get("name"));
    if (patch.containsKey("role")) me.setRole((String) patch.get("role"));
    return ResponseEntity.ok(users.save(me));
  }
}
