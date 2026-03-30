package org.jacorreu.embedding.application;

import org.jacorreu.embedding.application.dto.SaveEmbeddingCommand;
import org.jacorreu.embedding.core.domain.EmbeddingType;
import org.jacorreu.embedding.core.domain.valueobjects.EmbeddingContent;
import org.jacorreu.embedding.core.domain.valueobjects.EmbeddingUserId;
import org.jacorreu.embedding.core.gateway.EmbeddingGateway;
import org.jacorreu.shared.validation.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveEmbeddingUseCaseTest {

    @Mock
    private EmbeddingGateway embeddingGateway;

    @InjectMocks
    private SaveEmbeddingUseCase saveEmbeddingUseCase;

    @Test
    void execute_shouldSucceed_whenValidInput() {
        UUID userId = UUID.randomUUID();
        EmbeddingType type = EmbeddingType.ONBOARDING;
        String content = "test content";

        var command = new SaveEmbeddingCommand(userId, type, content);
        var result = saveEmbeddingUseCase.execute(command);

        assertTrue(result.isSuccess());
        verify(embeddingGateway).save(any(EmbeddingUserId.class), eq(type), any(EmbeddingContent.class));
    }

    @Test
    void execute_shouldReturnFailure_whenUserIdIsNull() {
        var command = new SaveEmbeddingCommand(null, EmbeddingType.ONBOARDING, "content");
        var result = saveEmbeddingUseCase.execute(command);

        assertFalse(result.isSuccess());
        assertTrue(result.getNotification().getErrors().stream()
                .anyMatch(e -> e.field().equals("userId")));
        verify(embeddingGateway, never()).save(any(), any(), any());
    }

    @Test
    void execute_shouldReturnFailure_whenTypeIsNull() {
        var command = new SaveEmbeddingCommand(UUID.randomUUID(), null, "content");
        var result = saveEmbeddingUseCase.execute(command);

        assertFalse(result.isSuccess());
        assertTrue(result.getNotification().getErrors().stream()
                .anyMatch(e -> e.field().equals("type")));
        verify(embeddingGateway, never()).save(any(), any(), any());
    }

    @Test
    void execute_shouldReturnFailure_whenContentIsNull() {
        var command = new SaveEmbeddingCommand(UUID.randomUUID(), EmbeddingType.ONBOARDING, null);
        var result = saveEmbeddingUseCase.execute(command);

        assertFalse(result.isSuccess());
        assertTrue(result.getNotification().getErrors().stream()
                .anyMatch(e -> e.field().equals("content")));
        verify(embeddingGateway, never()).save(any(), any(), any());
    }

    @Test
    void execute_shouldReturnFailure_whenContentIsBlank() {
        var command = new SaveEmbeddingCommand(UUID.randomUUID(), EmbeddingType.ONBOARDING, "   ");
        var result = saveEmbeddingUseCase.execute(command);

        assertFalse(result.isSuccess());
        assertTrue(result.getNotification().getErrors().stream()
                .anyMatch(e -> e.field().equals("content")));
        verify(embeddingGateway, never()).save(any(), any(), any());
    }
}
