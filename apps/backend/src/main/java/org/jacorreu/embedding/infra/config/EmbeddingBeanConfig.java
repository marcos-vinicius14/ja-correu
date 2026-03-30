package org.jacorreu.embedding.infra.config;

import org.jacorreu.embedding.application.RetrieveEmbeddingUseCase;
import org.jacorreu.embedding.application.SaveEmbeddingUseCase;
import org.jacorreu.embedding.core.gateway.EmbeddingGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingBeanConfig {

    @Bean
    public SaveEmbeddingUseCase saveEmbeddingUseCase(EmbeddingGateway embeddingGateway) {
        return new SaveEmbeddingUseCase(embeddingGateway);
    }

    @Bean
    public RetrieveEmbeddingUseCase retrieveEmbeddingUseCase(EmbeddingGateway embeddingGateway) {
        return new RetrieveEmbeddingUseCase(embeddingGateway);
    }
}
