package org.jacorreu.identity.infra.web.controllers;

import org.jacorreu.identity.application.dto.request.CreateUserRequest;
import org.jacorreu.identity.application.dto.request.LoginRequest;
import org.jacorreu.identity.application.dto.response.TokenResponse;
import org.jacorreu.identity.application.usecase.CreateUserUseCase;
import org.jacorreu.identity.application.usecase.LoginUseCase;
import org.jacorreu.identity.application.usecase.LogoutUseCase;
import org.jacorreu.identity.application.usecase.RenewTokenUseCase;
import org.jacorreu.identity.core.gateway.JwtGateway;
import org.jacorreu.identity.infra.web.dto.ErrorResponse;
import org.jacorreu.shared.validation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final Duration ACCESS_TOKEN_TTL = Duration.ofHours(10);
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
            return errorResponse(HttpStatus.BAD_REQUEST, "Erro ao criar usuário", result,
                    "Verifique se os dados estão corretos e tente novamente");
        }

        return ResponseEntity.status(201).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        log.debug("Login endpoint chamado para email: {}", request.email());

        Result<TokenResponse> result = loginUseCase.execute(request);

        log.debug("Login result success: {}", result.isSuccess());
        if (!result.isSuccess()) {
            log.debug("Login errors: {}", result.getNotification().getErrors());
            return errorResponse(HttpStatus.UNAUTHORIZED, "Erro ao realizar login", result,
                    "Verifique se os dados estão corretos e tente novamente");
        }

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE,
                        buildCookie("access_token", result.getData().accessToken(), ACCESS_TOKEN_TTL).toString())
                .header(HttpHeaders.SET_COOKIE,
                        buildCookie("refresh_token", result.getData().refreshToken(), REFRESH_TOKEN_TTL).toString())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(value = "access_token", required = false) String accessToken) {
        if (accessToken != null) {
            try {
                UUID userId = jwt.extractUserId(accessToken);
                logoutUseCase.execute(userId);
            } catch (Exception e) {
                log.debug("[logout] token inválido: {}", e.getMessage());
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildCookie("access_token", "", Duration.ZERO).toString())
                .header(HttpHeaders.SET_COOKIE, buildCookie("refresh_token", "", Duration.ZERO).toString())
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue("refresh_token") String refreshToken) {
        Result<TokenResponse> result = renewTokenUseCase.execute(UUID.fromString(refreshToken));

        if (!result.isSuccess()) {
            log.debug("Refresh errors: {}", result.getNotification().getErrors());
            return errorResponse(HttpStatus.UNAUTHORIZED, "Erro ao renovar credenciais", result,
                    "Realize o login novamente");
        }

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE,
                        buildCookie("access_token", result.getData().accessToken(), ACCESS_TOKEN_TTL).toString())
                .header(HttpHeaders.SET_COOKIE,
                        buildCookie("refresh_token", result.getData().refreshToken(), REFRESH_TOKEN_TTL).toString())
                .build();
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        return ResponseEntity.ok().build();
    }

    // ─── Helpers ─────────────────────────────────────────────

    private ResponseCookie buildCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
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
}