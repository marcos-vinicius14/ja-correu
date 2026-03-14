package org.jacorreu.identity.application.usecase;

import org.jacorreu.user.core.domain.UserDomain;
import org.jacorreu.user.core.domain.valueobjects.Email;
import org.jacorreu.user.core.gateway.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

public final class CustomUserDetailsUseCase implements UserDetailsService {

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
}

