package ru.taskmanagment.payload.rs;


import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
public class UserAuth implements UserDetails {
    private String email;
    private String password;
    // private List<String> roles;

    public UserAuth(@NotBlank String email, @NotBlank String password) {
        this.email = email;
        this.password = password != null ? password : "";
    }

    public UserAuth(String email) {
        this.email = email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
//        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
//        for (String role : roles) {
//            authorities.add(new SimpleGrantedAuthority(role));
//        }
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return this.password != null ? this.password : "";
    }

    @Override
    public String getUsername() {
        return this.email;
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
