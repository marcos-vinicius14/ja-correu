package org.jacorreu.onboarding.infra.web;

import org.jacorreu.onboarding.application.dto.AthleteProfileResult;
import org.jacorreu.onboarding.application.dto.CompleteOnboardingCommand;
import org.jacorreu.onboarding.application.usecase.CompleteOnboardingUseCase;
import org.jacorreu.onboarding.infra.web.dto.CompleteOnboardingRequest;
import org.jacorreu.onboarding.infra.web.dto.OnboardingResponse;
import org.jacorreu.shared.dto.ApiResponse;
import org.jacorreu.shared.validation.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {

    private final CompleteOnboardingUseCase completeOnboardingUseCase;

    public OnboardingController(CompleteOnboardingUseCase completeOnboardingUseCase) {
        this.completeOnboardingUseCase = completeOnboardingUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OnboardingResponse>> completeOnboarding(
            @RequestBody CompleteOnboardingRequest request) {
        var principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof UUID userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("authentication", "Token invalido ou expirado"));
        }

        var command = new CompleteOnboardingCommand(
                userId, request.goal(), request.level(),
                request.availableDaysPerWeek(), request.currentPacePerKm(),
                request.injuriesNotes()
        );

        return toResponse(completeOnboardingUseCase.execute(command));
    }

    private ResponseEntity<ApiResponse<OnboardingResponse>> toResponse(
            Result<AthleteProfileResult> result) {
        return result.isSuccess()
                ? ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(OnboardingResponse.from(result.getData())))
                : ResponseEntity.status(resolveHttpStatus(result))
                        .body(ApiResponse.error(result.getNotification().getErrors().stream()
                                .map(e -> new ApiResponse.ErrorDto(e.field(), e.message()))
                                .toList()));
    }

    private HttpStatus resolveHttpStatus(Result<AthleteProfileResult> result) {
        return result.getNotification().getErrors().stream()
                .anyMatch(e -> "already_active".equals(e.field()))
                ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
    }
}
