package org.jacorreu.user.infraestructure.persistence.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_users")
public class UserJpaEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, name = "username")
    private String name;

    @Column(nullable = false, name = "password")
    private String password;

    @Column(nullable = false, name = "email", unique = true)
    private String email;

    @Column(name = "strava_token", unique = true)
    private String stravaToken;

    @Column(nullable = false, name = "created_at")
    @CreatedDate
    private Instant createdAt;

    protected UserJpaEntity() {
    }


}
