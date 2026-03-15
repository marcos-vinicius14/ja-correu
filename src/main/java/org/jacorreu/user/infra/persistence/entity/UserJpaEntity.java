package org.jacorreu.user.infra.persistence.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_users")
public class UserJpaEntity {
    @Id
    private UUID id;

    @Column(nullable = false, name = "username")
    private String name;

    @Column(nullable = false, name = "password")
    private String password;

    @Column(nullable = false, name = "email", unique = true)
    private String email;

    @Column(name = "strava_acess_token", unique = true)
    private String stravaTokenAcessToken;

    @Column(name = "strava_refresh_token", unique = true)
    private String stravaRefreshToken;

    @Column(name = "strava_expires_at")
    private Long strava_expires_at;

    @Column(nullable = false, name = "created_at")
    @CreatedDate
    private Instant createdAt;

    protected UserJpaEntity() {
    }

    public UserJpaEntity(
            UUID id,
            String name,
            String password,
            String email,
            String stravaTokenAcessToken,
            String stravaRefreshToken,
            Long strava_expires_at
    ) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.email = email;
        this.stravaTokenAcessToken = stravaTokenAcessToken;
        this.stravaRefreshToken = stravaRefreshToken;
        this.strava_expires_at = strava_expires_at;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getStravaTokenAcessToken() {
        return stravaTokenAcessToken;
    }

    public String getStravaRefreshToken() {
        return stravaRefreshToken;
    }

    public Long getStrava_expires_at() {
        return strava_expires_at;
    }
}
