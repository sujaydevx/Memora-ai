package com.memora.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseV2 {
    private String token;
    private UUID userId;
    private String email;
    private String displayName;
}