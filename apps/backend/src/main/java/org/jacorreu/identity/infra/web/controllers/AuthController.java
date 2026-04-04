package org.jacorreu.identity.infra.web.controllers;

import org.jacorreu.identity.application.dto.request.CreateUserRequest;
import org.jacorreu.identity.application.dto.request.LoginRequest;
import org.jacorreu.identity.application.dto.response.TokenResponse;
import org.jacorreu.identity.application.usecase.CreateUserUseCase;
import org.jacorreu.identity.application.usecase.LoginUseCase;
import org.jacorreu.identity.application.usecase.LogoutUseCase;
import org.jacorreu.identity.application.usecase.RenewTokenUseCase;
import org.jacorreu.identity.infra.web.dto.AuthTokenResponse;
import org.jacorreu.shared.validation.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

        private static final Logger log = LoggerFactory.getLogger(AuthController.class);
        private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(7);

        private final RenewTokenUseCase renewTokenUseCase;
        private final LoginUseCase loginUseCase;
        private final LogoutUseCase logoutUseCase;
        private final CreateUserUseCase createUserUseCase;

        public AuthController(RenewTokenUseCase renewTokenUseCase,
                        LoginUseCase loginUseCase,
                        LogoutUseCase logoutUseCase,
                        CreateUserUseCase createUserUseCase) {
                this.renewTokenUseCase = renewTokenUseCase;
                this.loginUseCase = loginUseCase;
                this.logoutUseCase = logoutUseCase;
                this.createUserUseCase = createUserUseCase;
        }

        @PostMapping(value = "/register", produces = MediaTypes.HAL_JSON_VALUE)
        public ResponseEntity<?> create(@RequestBody CreateUserRequest request) {
                Result<Void> result = createUserUseCase.execute(request);

                if (!result.isSuccess()) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(problemDetail(HttpStatus.BAD_REQUEST, "Erro ao criar usuario",
                                                        result, "/api/v1/auth/register"));
                }

                var body = new AuthTokenResponse(null);
                body.add(linkTo(methodOn(AuthController.class).login(null)).withRel("login"));

                return ResponseEntity.status(HttpStatus.CREATED)
                                .contentType(MediaTypes.HAL_JSON)
                                .body(body);
        }

        @PostMapping(value = "/login", produces = MediaTypes.HAL_JSON_VALUE)
        public ResponseEntity<?> login(@RequestBody LoginRequest request) {
                Result<TokenResponse> result = loginUseCase.execute(request);

                log.debug("Login endpoint chamado para email: {}", request == null ? null : request.email());

                if (!result.isSuccess()) {
                        log.debug("Login errors: {}", result.getNotification().getErrors());
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(problemDetail(HttpStatus.UNAUTHORIZED, "Erro ao realizar login",
                                                        result, "/api/v1/auth/login"));
                }

                var body = new AuthTokenResponse(result.getData().accessToken());
                body.add(linkTo(methodOn(AuthController.class).login(null)).withSelfRel());
                body.add(linkTo(methodOn(AuthController.class).logout()).withRel("logout"));
                body.add(linkTo(methodOn(AuthController.class).refresh(null)).withRel("refresh"));

                return ResponseEntity.ok()
                                .contentType(MediaTypes.HAL_JSON)
                                .header(HttpHeaders.SET_COOKIE,
                                                buildRefreshCookie(result.getData().refreshToken(), REFRESH_TOKEN_TTL)
                                                                .toString())
                                .body(body);
        }

        @PostMapping(value = "/logout", produces = MediaTypes.HAL_JSON_VALUE)
        public ResponseEntity<?> logout() {
                var userId = (UUID) SecurityContextHolder.getContext()
                                .getAuthentication().getPrincipal();

                logoutUseCase.execute(userId);

                var body = new AuthTokenResponse(null);
                body.add(linkTo(methodOn(AuthController.class).login(null)).withRel("login"));

                return ResponseEntity.ok()
                                .contentType(MediaTypes.HAL_JSON)
                                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie("", Duration.ZERO).toString())
                                .body(body);
        }

        @PostMapping(value = "/refresh", produces = MediaTypes.HAL_JSON_VALUE)
        public ResponseEntity<?> refresh(@CookieValue("refresh_token") String refreshToken) {
                Result<TokenResponse> result = renewTokenUseCase.execute(UUID.fromString(refreshToken));

                if (!result.isSuccess()) {
                        log.debug("Refresh errors: {}", result.getNotification().getErrors());
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(problemDetail(HttpStatus.UNAUTHORIZED, "Erro ao renovar credenciais",
                                                        result, "/api/v1/auth/refresh"));
                }

                var body = new AuthTokenResponse(result.getData().accessToken());
                body.add(linkTo(methodOn(AuthController.class).refresh(null)).withSelfRel());
                body.add(linkTo(methodOn(AuthController.class).logout()).withRel("logout"));

                return ResponseEntity.ok()
                                .contentType(MediaTypes.HAL_JSON)
                                .header(HttpHeaders.SET_COOKIE,
                                                buildRefreshCookie(result.getData().refreshToken(), REFRESH_TOKEN_TTL)
                                                                .toString())
                                .body(body);
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

        private ProblemDetail problemDetail(HttpStatus status, String title, Result<?> result, String instance) {
                var detail = result.getNotification().getErrors().isEmpty()
                                ? title
                                : result.getNotification().getErrors().getFirst().message();
                var pd = ProblemDetail.forStatusAndDetail(status, detail);
                pd.setTitle(title);
                pd.setType(URI.create("https://jacorreu.org/errors/" + status.name().toLowerCase()));
                pd.setInstance(URI.create(instance));
                return pd;
        }
}
