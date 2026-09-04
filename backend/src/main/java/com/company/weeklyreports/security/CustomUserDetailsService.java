package com.company.weeklyreports.security;

import com.company.weeklyreports.model.entity.User;
import com.company.weeklyreports.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Bridges Spring Security's authentication mechanism to our own User table.
 * "Username" here is always the email, since that's the login identifier -
 * used both by AuthenticationManager during login (via the auto-configured
 * DaoAuthenticationProvider) and by JwtAuthFilter on every subsequent
 * request (via the email claim carried in the JWT).
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));
        return new CustomUserPrincipal(user);
    }
}
