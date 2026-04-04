package org.jacorreu.onboarding.infra.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.jacorreu.identity.core.gateway.ExtendsUserDetailsService;
import org.jacorreu.identity.infra.config.SecurityConfig;
import org.jacorreu.identity.infra.config.TraceIdFilter;
import org.jacorreu.identity.infra.security.JwtAuthFilter;
import org.jacorreu.onboarding.application.dto.AthleteProfileResult;
import org.jacorreu.onboarding.application.usecase.CompleteOnboardingUseCase;
import org.jacorreu.onboarding.application.usecase.GetAthleteProfileUseCase;
import org.jacorreu.onboarding.core.domain.valueobjects.Goal;
import org.jacorreu.onboarding.core.domain.valueobjects.Level;
import org.jacorreu.shared.validation.Notification;
import org.jacorreu.shared.validation.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = OnboardingController.class)
@Import(SecurityConfig.class)
class OnboardingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompleteOnboardingUseCase completeOnboardingUseCase;

    @MockitoBean
    private GetAthleteProfileUseCase getAthleteProfileUseCase;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private TraceIdFilter traceIdFilter;

    @MockitoBean
    private ExtendsUserDetailsService userDetailsService;

    private static final UUID PROFILE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @BeforeEach
    void configureFilterPassthrough() throws Exception {
        doAnswer(
                        inv -> {
                            inv.getArgument(2, FilterChain.class)
                                    .doFilter(
                                            inv.getArgument(0, HttpServletRequest.class),
                                            inv.getArgument(1, HttpServletResponse.class));
                            return null;
                        })
                .when(jwtAuthFilter)
                .doFilter(any(), any(), any());
        doAnswer(
                        inv -> {
                            inv.getArgument(2, FilterChain.class)
                                    .doFilter(
                                            inv.getArgument(0, HttpServletRequest.class),
                                            inv.getArgument(1, HttpServletResponse.class));
                            return null;
                        })
                .when(traceIdFilter)
                .doFilter(any(), any(), any());
    }

    private AthleteProfileResult sampleResult() {
        return new AthleteProfileResult(
                PROFILE_ID, USER_ID, Goal.MARATHON, Level.ADVANCED, 5, "05:00", "Nenhuma lesao");
    }

    private UsernamePasswordAuthenticationToken authenticatedAs(UUID userId) {
        return new UsernamePasswordAuthenticationToken(userId, null, java.util.List.of());
    }

    // ─── POST /api/v1/onboarding ──────────────────────────────────────────────

    @Test
    void completeOnboarding_validRequest_returns201WithHalBody() throws Exception {
        when(completeOnboardingUseCase.execute(any())).thenReturn(Result.success(sampleResult()));

        mockMvc.perform(post("/api/v1/onboarding")
                        .with(authentication(authenticatedAs(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(
                                """
                                {
                                  "goal": "MARATHON",
                                  "level": "ADVANCED",
                                  "availableDaysPerWeek": 5,
                                  "currentPacePerKm": "05:00",
                                  "injuriesNotes": "Nenhuma lesao"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.data.id").value(PROFILE_ID.toString()))
                .andExpect(jsonPath("$.data.userId").value(USER_ID.toString()))
                .andExpect(jsonPath("$.data.goal").value("MARATHON"))
                .andExpect(jsonPath("$.data.level").value("ADVANCED"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void completeOnboarding_nonUuidPrincipal_returns401() throws Exception {
        var nonUuidAuth = new UsernamePasswordAuthenticationToken("string-principal", null, java.util.List.of());

        mockMvc.perform(post("/api/v1/onboarding")
                        .with(authentication(nonUuidAuth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(
                                """
                                {
                                  "goal": "MARATHON",
                                  "level": "ADVANCED",
                                  "availableDaysPerWeek": 5,
                                  "currentPacePerKm": "05:00",
                                  "injuriesNotes": "Nenhuma lesao"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Nao autorizado"))
                .andExpect(jsonPath("$.detail").value("Token invalido ou expirado"));
    }

    @Test
    void completeOnboarding_alreadyActive_returns409() throws Exception {
        var notification = new Notification().addError("already_active", "Onboarding ja foi concluido");
        when(completeOnboardingUseCase.execute(any())).thenReturn(Result.failure(notification));

        mockMvc.perform(post("/api/v1/onboarding")
                        .with(authentication(authenticatedAs(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(
                                """
                                {
                                  "goal": "MARATHON",
                                  "level": "ADVANCED",
                                  "availableDaysPerWeek": 5,
                                  "currentPacePerKm": "05:00",
                                  "injuriesNotes": "Nenhuma lesao"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflito"))
                .andExpect(jsonPath("$.detail").value("Onboarding ja foi concluido"));
    }

    @Test
    void completeOnboarding_validationError_returns422() throws Exception {
        var notification = new Notification().addError("availableDaysPerWeek", "Deve ser entre 1 e 7");
        when(completeOnboardingUseCase.execute(any())).thenReturn(Result.failure(notification));

        mockMvc.perform(post("/api/v1/onboarding")
                        .with(authentication(authenticatedAs(USER_ID)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(
                                """
                                {
                                  "goal": "MARATHON",
                                  "level": "ADVANCED",
                                  "availableDaysPerWeek": 99,
                                  "currentPacePerKm": "05:00",
                                  "injuriesNotes": ""
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Erro de validacao"))
                .andExpect(jsonPath("$.errors[0].field").value("availableDaysPerWeek"));
    }

    // ─── GET /api/v1/onboarding/{id} ─────────────────────────────────────────

    @Test
    void getAthleteProfile_exists_returns200WithHalBody() throws Exception {
        when(getAthleteProfileUseCase.execute(PROFILE_ID)).thenReturn(Result.success(sampleResult()));

        mockMvc.perform(get("/api/v1/onboarding/{id}", PROFILE_ID)
                        .with(authentication(authenticatedAs(USER_ID)))
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.data.id").value(PROFILE_ID.toString()))
                .andExpect(jsonPath("$.data.goal").value("MARATHON"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void getAthleteProfile_notFound_returns404() throws Exception {
        var notification = new Notification().addError("id", "Perfil nao encontrado");
        when(getAthleteProfileUseCase.execute(PROFILE_ID)).thenReturn(Result.failure(notification));

        mockMvc.perform(get("/api/v1/onboarding/{id}", PROFILE_ID)
                        .with(authentication(authenticatedAs(USER_ID)))
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Recurso nao encontrado"))
                .andExpect(jsonPath("$.detail").value("Perfil de atleta nao encontrado"));
    }
}
