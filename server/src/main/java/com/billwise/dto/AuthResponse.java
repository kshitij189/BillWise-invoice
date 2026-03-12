package com.billwise.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private Map<String, Object> result;
    private String token;
    private Object userProfile;
}
