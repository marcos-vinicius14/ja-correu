package org.jacorreu.onboarding.core.gateway;

import org.jacorreu.onboarding.core.domain.AthleteProfileDomain;

public interface OnboardingEmbeddingGateway {
    void generateOnboardingEmbedding(AthleteProfileDomain profile);
}
