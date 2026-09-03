package com.company.weeklyreports.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response returned after a successful login/register - the JWT the
 * frontend will attach as a Bearer token, plus the caller's own profile so
 * the UI doesn't need a second round trip just to know who's logged in.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;

    private UserDTO user;
}
