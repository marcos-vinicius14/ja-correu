package org.jacorreu.identity.application.dto.request;

import org.jacorreu.user.core.domain.valueobjects.Email;
import org.jacorreu.user.core.domain.valueobjects.Password;

public record CreateUserRequest(
        String username,
        String email,
        String password
) {
}
