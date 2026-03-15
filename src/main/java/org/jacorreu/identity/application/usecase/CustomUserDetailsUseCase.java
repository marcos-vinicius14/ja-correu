package org.jacorreu.identity.application.usecase;

import jakarta.persistence.EntityNotFoundException;
import org.jacorreu.identity.core.gateway.ExtendsUserDetailsService;
import org.jacorreu.shared.validation.Notification;
import org.jacorreu.user.core.domain.UserDomain;
import org.jacorreu.user.core.domain.valueobjects.Email;
import org.jacorreu.user.core.gateway.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

public final class CustomUserDetailsUseCase implements ExtendsUserDetailsService {

    private final UserRepository repository;

    public CustomUserDetailsUseCase(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserDomain user = repository.findByEmail(Email.restore(email))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado!"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail().getValue())
                .password(user.getPassword().getValue())
                .roles("USER")
                .build();
     }

    @Override
    public UserDetails loadUserById(UUID userId) {
        UserDomain user = repository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario nao encontrado!"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail().getValue())
                .password(user.getPassword().getValue())
                .roles("USER")
                .build();
    }
}

