package com.blackcode.auth_service.security.service;

import com.blackcode.auth_service.model.UserAuth;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UserAuthDetailsImpl implements UserDetails {

    private static final long serialVersionUID = 1L;

    private String userId;

    private String username;

    @JsonIgnore
    private String password;

    private final Collection<? extends GrantedAuthority> authorities;

    public UserAuthDetailsImpl(String userId, String username, String password) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.authorities = Collections.emptyList();
    }

    public static UserAuthDetailsImpl build(UserAuth userAuth) {
        return new UserAuthDetailsImpl(
                userAuth.getUserId(),
                userAuth.getUsername(),
                userAuth.getPassword()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }


    public String getUserId() {
        return userId;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserAuthDetailsImpl userAuth = (UserAuthDetailsImpl) o;
        return Objects.equals(userId, userAuth.userId);
    }
}
