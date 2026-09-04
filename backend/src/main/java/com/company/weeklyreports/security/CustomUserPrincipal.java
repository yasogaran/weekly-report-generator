package com.company.weeklyreports.security;

import com.company.weeklyreports.model.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapts a User entity to Spring Security's UserDetails contract, wrapping
 * the full entity rather than just the username/authorities. Once
 * JwtAuthFilter loads one of these per request, every downstream layer
 * (controllers included) can read the caller's id/name/role straight off
 * getUser() - no second database lookup needed just to find out who's
 * calling.
 */
public class CustomUserPrincipal implements UserDetails {

    private final User user;

    public CustomUserPrincipal(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    // Exactly "ROLE_" + role name - this precise prefix is what Spring
    // Security's hasRole('MANAGER')/hasRole('TEAM_MEMBER') expressions
    // expect under the hood (hasRole(x) is shorthand that checks for
    // authority "ROLE_" + x).
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    // No expiry/locking rules exist in this system yet - only the User
    // Management task's deactivate flow (User.isActive) affects usability.
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Wired to User.isActive (added by the User Management task) rather
    // than hardcoded true: this is what makes a manager deactivating a
    // user actually take effect, both at login (DaoAuthenticationProvider
    // checks isEnabled() automatically and rejects a disabled account with
    // DisabledException) and on every subsequent request (see
    // JwtAuthFilter, which re-checks this on the freshly-loaded
    // UserDetails before trusting an existing token).
    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
}
