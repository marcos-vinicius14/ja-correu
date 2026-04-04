package org.jacorreu.onboarding.infra.web;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import org.jacorreu.onboarding.application.dto.AthleteProfileResult;
import org.jacorreu.onboarding.application.dto.CompleteOnboardingCommand;
import org.jacorreu.onboarding.application.usecase.CompleteOnboardingUseCase;
import org.jacorreu.onboarding.application.usecase.GetAthleteProfileUseCase;
import org.jacorreu.onboarding.infra.web.dto.CompleteOnboardingRequest;
import org.jacorreu.onboarding.infra.web.dto.OnboardingResponse;
import org.jacorreu.shared.dto.ApiResponse;
import org.jacorreu.shared.dto.HalResponse;
import org.jacorreu.shared.validation.Result;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {

    private static final String ERROR_TYPE_BASE = "https://jacorreu.org/errors/";

    private final CompleteOnboardingUseCase completeOnboardingUseCase;
    private final GetAthleteProfileUseCase getAthleteProfileUseCase;

    public OnboardingController(
            CompleteOnboardingUseCase completeOnboardingUseCase,
            GetAthleteProfileUseCase getAthleteProfileUseCase) {
        this.completeOnboardingUseCase = completeOnboardingUseCase;
        this.getAthleteProfileUseCase = getAthleteProfileUseCase;
    }

    @PostMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> completeOnboarding(@RequestBody CompleteOnboardingRequest request) {
        return resolveUserId()
                .<ResponseEntity<?>>map(userId -> executeOnboarding(userId, request))
                .orElseGet(this::unauthorizedResponse);
    }

    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public ResponseEntity<?> getAthleteProfile(@PathVariable UUID id) {
        var result = getAthleteProfileUseCase.execute(id);

        return result.isSuccess()
                ? okHalResponse(OnboardibngResponse.from(result.getData()), result.getData().id())
                : notFoundResponse(id);
    }

    private Optional<UUID> resolveUserId() {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof UUID userId
                ? Optional.of(userId)
                : Optional.empty();
    }

    private ResponseEntity<?> executeOnboarding(UUID userId, CompleteOnboardingRequest request) {
        var result = completeOnboardingUseCase.execute(toCommand(userId, request));

        return result.isSuccess()
                ? createdHalResponse(result.getData())
                : errorResponse(result);
    }

    private CompleteOnboardingCommand toCommand(UUID userId, CompleteOnboardingRequest request) {
        return new CompleteOnboardingCommand(
                userId,
                request.goal(),
                request.level(),
                request.availableDaysPerWeek(),
                request.currentPacePerKm(),
                request.injuriesNotes());
    }

    private ResponseEntity<HalResponse<OnboardingResponse>> createdHalResponse(
            AthleteProfileResult data) {
        var response = OnboardingResponse.from(data);
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaTypes.HAL_JSON)
                .body(toHal(response, response.id()));
    }

    private ResponseEntity<HalResponse<OnboardingResponse>> okHalResponse(
            OnboardingResponse response, UUID id) {
        return ResponseEntity.ok().contentType(MediaTypes.HAL_JSON).body(toHal(response, id));
    }

    private HalResponse<OnboardingResponse> toHal(OnboardingResponse response, UUID id) {
        var hal = HalResponse.of(ApiResponse.success(response));
        hal.add(linkTo(methodOn(OnboardingController.class).getAthleteProfile(id)).withSelfRel());
        return hal;
    }

    private ResponseEntity<ProblemDetail> errorResponse(Result<AthleteProfileResult> result) {
        var status = resolveStatus(result);
        return ResponseEntity.status(status)
                .body(buildProblemDetail(status, result, "/api/v1/onboarding"));
    }

    private HttpStatus resolveStatus(Result<AthleteProfileResult> result) {
        return result.getNotification().getErrors().stream()
                .anyMatch(e -> "already_active".equals(e.field()))
                        ? HttpStatus.CONFLICT
                        : HttpStatus.UNPROCESSABLE_ENTITY;
    }

    private ProblemDetail buildProblemDetail(
            HttpStatus status, Result<AthleteProfileResult> result, String instance) {
        var detail = result.getNotification().getErrors().isEmpty()
                ? "Erro de validacao"
                : result.getNotification().getErrors().getFirst().message();
        var pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(status == HttpStatus.CONFLICT ? "Conflito" : "Erro de validacao");
        pd.setType(URI.create(ERROR_TYPE_BASE + status.name().toLowerCase()));
        pd.setInstance(URI.create(instance));
        pd.setProperty(
                "errors",
                result.getNotification().getErrors().stream()
                        .map(e -> new ApiResponse.ErrorDto(e.field(), e.message()))
                        .toList());
        return pd;
    }

    private ResponseEntity<ProblemDetail> unauthorizedResponse() {
        var pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, "Token invalido ou expirado");
        pd.setTitle("Nao autorizado");
        pd.setType(URI.create(ERROR_TYPE_BASE + "unauthorized"));
        pd.setInstance(URI.create("/api/v1/onboarding"));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(pd);
    }

    private ResponseEntity<ProblemDetail> notFoundResponse(UUID id) {
        var pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, "Perfil de atleta nao encontrado");
        pd.setTitle("Recurso nao encontrado");
        pd.setType(URI.create(ERROR_TYPE_BASE + "not_found"));
        pd.setInstance(URI.create("/api/v1/onboarding/" + id));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }
}
