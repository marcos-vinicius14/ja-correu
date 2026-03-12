package org.jacorreu.identity.infra.persistence.repository;

import jakarta.transaction.Transactional;
import org.jacorreu.identity.infra.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    Optional<RefreshTokenEntity> findByTokenId(UUID uuid);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshTokenEntity p SET p.isRevoked = true WHERE p.tokenId = :tokenId")
    int revokeToken(@Param("tokenId") UUID tokenId);
}
