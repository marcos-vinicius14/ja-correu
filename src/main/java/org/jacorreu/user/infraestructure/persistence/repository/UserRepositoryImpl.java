package org.jacorreu.user.infraestructure.persistence.repository;

import org.jacorreu.user.core.domain.UserDomain;
import org.jacorreu.user.core.domain.valueobjects.Email;
import org.jacorreu.user.gateway.UserRepository;
import org.jacorreu.user.infraestructure.persistence.entity.UserJpaEntity;
import org.jacorreu.user.infraestructure.persistence.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final SpringDataUserRepository repository;
    private final UserMapper mapper;

    public UserRepositoryImpl(SpringDataUserRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void save(UserDomain user) {
        UserJpaEntity userToSave = mapper.toEntity(user);
        repository.save(userToSave);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);

    }

    @Override
    public Optional<UserDomain> findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<UserDomain> findByEmail(Email email) {
        return repository.findByEmail(email.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return repository.existsByEmail(email.getValue());
    }
}
