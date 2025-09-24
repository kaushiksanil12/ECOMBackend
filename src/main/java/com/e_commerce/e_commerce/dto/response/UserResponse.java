package com.e_commerce.e_commerce.dto.response;

import com.e_commerce.e_commerce.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private String phone;
    private String address;
    private boolean enabled;
    private LocalDateTime createdDate;
}
