package org.jacorreu.identity.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import org.jacorreu.user.core.domain.UserDomain;
import org.jacorreu.user.core.domain.valueobjects.Email;
import org.jacorreu.user.core.gateway.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsUseCase customUserDetailsUseCase;

    private static final String EMAIL = "test@example.com";
    private static final String ENCODED_PASSWORD = "encodedPassword";

    private UserDomain buildUser(UUID id) {
        return UserDomain.restore(id, "testuser", EMAIL, ENCODED_PASSWORD, null, null, null);
    }

    @Test
    void loadUserByUsername_success_returnsUserDetails() {
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(buildUser(UUID.randomUUID())));

        UserDetails result = customUserDetailsUseCase.loadUserByUsername(EMAIL);

        assertEquals(EMAIL, result.getUsername());
        assertEquals(ENCODED_PASSWORD, result.getPassword());
    }

    @Test
    void loadUserByUsername_userNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsUseCase.loadUserByUsername(EMAIL));
    }

    @Test
    void loadUserById_success_returnsUserDetails() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(buildUser(userId)));

        UserDetails result = customUserDetailsUseCase.loadUserById(userId);

        assertEquals(EMAIL, result.getUsername());
        assertEquals(ENCODED_PASSWORD, result.getPassword());
    }

    @Test
    void loadUserById_userNotFound_throwsEntityNotFoundException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> customUserDetailsUseCase.loadUserById(userId));
    }
}
