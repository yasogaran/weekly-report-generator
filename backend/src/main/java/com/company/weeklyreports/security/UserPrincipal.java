package com.company.weeklyreports.security;

import com.company.weeklyreports.model.entity.Role;
import com.company.weeklyreports.model.entity.User;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Adapts our domain User entity to Spring Security's UserDetails contract. Wrapping (not
 * extending/reusing User directly) keeps User a plain JPA entity with no framework coupling,
 * while still giving controllers easy access to the real domain id via {@link #getId()}
 * instead of parsing it back out of the username/authorities.
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String name;
    private final String email;
    private final String passwordHash;
    private final boolean active;
    private final Role role;
    private final Collection<GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.active = user.isActive();
        this.role = user.getRole();
        // Spring Security convention: role names carry a "ROLE_" prefix for hasRole()/@PreAuthorize("hasRole(...)").
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    /** Used by services that need "is this a manager" for ownership-vs-role-based access (e.g. ReportAccessGuard). */
    public boolean isManager() {
        return role == Role.MANAGER;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    /** Checked on every authenticated request (JwtAuthFilter), not just at login — api-doc.md. */
    @Override
    public boolean isEnabled() {
        return active;
    }

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
}
