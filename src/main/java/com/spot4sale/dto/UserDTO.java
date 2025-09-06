
package com.spot4sale.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private UUID id;
    private String name;
    private String email;
    private String role; // USER or STORE_OWNER
    private String phone;
    private String profileImage;
    private Boolean chargesEnabled;
    private String stripeAccountId;
    private Boolean isActive;
}
