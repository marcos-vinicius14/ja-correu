package org.jacorreu.identity.infra.web.controllers;

import org.jacorreu.identity.application.dto.request.CreateUserRequest;
import org.jacorreu.identity.application.dto.request.LoginRequest;
import org.jacorreu.identity.application.dto.response.TokenResponse;
import org.jacorreu.identity.application.usecase.CreateUserUseCase;
import org.jacorreu.identity.application.usecase.LoginUseCase;
import org.jacorreu.identity.application.usecase.LogoutUseCase;
import org.jacorreu.identity.application.usecase.RenewTokenUseCase;
import org.jacorreu.identity.core.gateway.JwtGateway;
import org.jacorreu.identity.infra.security.JwtAuthFilter;
import org.jacorreu.identity.infra.web.dto.ErrorResponse;
import org.jacorreu.shared.validation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

    private final RenewTokenUseCase renewTokenUseCase;
    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final JwtGateway jwt;

    public AuthController(RenewTokenUseCase renewTokenUseCase,
                          LoginUseCase loginUseCase,
                          LogoutUseCase logoutUseCase,
                          CreateUserUseCase createUserUseCase,
                          JwtGateway jwt) {
        this.renewTokenUseCase = renewTokenUseCase;
        this.loginUseCase = loginUseCase;
        this.logoutUseCase = logoutUseCase;
        this.createUserUseCase = createUserUseCase;
        this.jwt = jwt;
    }


    @PostMapping("/register")
    public ResponseEntity<?> create(@RequestBody CreateUserRequest request) {
        Result<Void> result = createUserUseCase.execute(request);

        if (!result.isSuccess()) {
            return errorResponse(HttpStatus.BAD_REQUEST, "Erro ao criar usuário", result, "Verifique se os dados estão corretos e tente novamente");
        }

        return ResponseEntity.status(201).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Result<TokenResponse> result = loginUseCase.execute(request);

        log.debug("Login endpoint chamado para email: {}", request.email());
        log.debug("Login endpoint chamado");

        log.debug("Login result success: {}", result.isSuccess());
        if (!result.isSuccess()) {
            log.debug("Login errors: {}", result.getNotification().getErrors());

            return errorResponse(HttpStatus.UNAUTHORIZED, "Erro ao realizar login", result, "Verifique se os dados estão corretos e tente novamente");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.getData().refreshToken(), REFRESH_TOKEN_TTL).toString())
                .body(Map.of("accessToken", result.getData().accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        String token = extractBearerToken(authHeader);

        if (token == null) {
            return errorResponse(HttpStatus.BAD_REQUEST, "Token inválido", "Header Authorization ausente ou mal formatado", "Envie o header no formato: Bearer <token>");
        }

        try {
            UUID userId = jwt.extractUserId(token);
            logoutUseCase.execute(userId);
        } catch (Exception e) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "Token inválido", e.getMessage(), "Realize o login novamente");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie("", Duration.ZERO).toString())
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue("refresh_token") String refreshToken) {
        Result<TokenResponse> result = renewTokenUseCase.execute(UUID.fromString(refreshToken));

        if (!result.isSuccess()) {
            log.debug("Login errors: {}", result.getNotification().getErrors());
            return errorResponse(HttpStatus.UNAUTHORIZED, "Erro ao renovar credenciais", result, "Realize o login novamente");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(result.getData().refreshToken(), REFRESH_TOKEN_TTL).toString())
                .body(Map.of("accessToken", result.getData().accessToken()));
    }

    // ─── Helpers ─────────────────────────────────────────────

    private ResponseCookie buildRefreshCookie(String value, Duration maxAge) {
        return ResponseCookie.from("refresh_token", value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Strict")
                .build();
    }

    private ResponseEntity<?> errorResponse(HttpStatus status, String title, Result<?> result, String hint) {
        String detail = result.getNotification().getErrors().isEmpty()
                ? title
                : result.getNotification().getErrors().getFirst().message();

        return ResponseEntity.status(status).body(new ErrorResponse(title, detail, hint));
    }

    private ResponseEntity<?> errorResponse(HttpStatus status, String title, String detail, String hint) {
        return ResponseEntity.status(status).body(new ErrorResponse(title, detail, hint));
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.toLowerCase().startsWith("bearer ")) {
            return null;
        }
        return authHeader.substring(7).trim();
    }
}