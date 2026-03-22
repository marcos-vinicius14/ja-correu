package org.jacorreu.identity.core.gateway;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.UUID;

public interface ExtendsUserDetailsService extends UserDetailsService {
    UserDetails loadUserById(UUID userId);
}
