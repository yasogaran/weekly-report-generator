package com.company.weeklyreports.security;

import com.company.weeklyreports.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security's hook for "given a username (email, here), load the account." Used both
 * by the login flow (AuthenticationManager calls this) and by JwtAuthFilter (re-loading the
 * user fresh from the DB on every request, so a deactivated account is caught immediately
 * rather than trusting stale claims baked into an already-issued token).
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("No account with email " + email));
    }
}
