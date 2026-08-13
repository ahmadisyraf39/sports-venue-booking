package com.ahmadisyraf39.sportsbooking.user_service.dto.response;

import com.ahmadisyraf39.sportsbooking.user_service.entity.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private String phone;
    private Role role;
    private boolean enabled;
    private LocalDateTime createdAt;
}
