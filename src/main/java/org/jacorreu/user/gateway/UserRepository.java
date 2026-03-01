package org.jacorreu.user.gateway;

import org.jacorreu.user.core.domain.UserDomain;
import org.jacorreu.user.core.domain.valueobjects.Email;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    void save(UserDomain user);
    void deleteById(UUID id);
    Optional<UserDomain> findById(UUID id);
    Optional<UserDomain> findByEmail(Email email);
    boolean existsByEmail(Email email);
}
