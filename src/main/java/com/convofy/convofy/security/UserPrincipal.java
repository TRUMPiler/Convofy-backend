package com.convofy.convofy.security;

import com.convofy.convofy.Entity.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class UserPrincipal implements UserDetails {

    @Getter
    private final UUID userId;
    @Getter
    private final String email;
    @Getter
    private final String password; // Stored hashed password
    @Getter
    private final String name;
    @Getter
    private final String image;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.password = user.getPassword(); // Assuming User entity has getPassword()
        this.name = user.getName();
        this.image = user.getImage();
        // Assign a default role. You can make this dynamic if your User entity has roles.
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email; // Use email as the username for UserDetails
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

    @Override
    public boolean isEnabled() {
        return true;
    }
}
