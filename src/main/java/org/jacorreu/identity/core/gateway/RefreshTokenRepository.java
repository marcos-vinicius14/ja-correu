package org.jacorreu.identity.core.gateway;

import org.jacorreu.identity.core.domain.RefreshTokenDomain;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
    void save(RefreshTokenDomain domain);
    Optional<RefreshTokenDomain> findByTokenId(UUID tokenId);
    void revoke(UUID tokenId);
}
