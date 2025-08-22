
package com.spot4sale.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Table(name = "users")
public class User {
  @Id @GeneratedValue private UUID id;
  @Column(nullable=false) private String name;
  @Column(nullable=false, unique=true) private String email;
  @Column(nullable=false) private String role; // USER | STORE_OWNER | ADMIN
  private String authProvider; // GOOGLE | FACEBOOK | LOCAL
  private String stripeAccountId; // acct_...
  private Boolean chargesEnabled;
}
