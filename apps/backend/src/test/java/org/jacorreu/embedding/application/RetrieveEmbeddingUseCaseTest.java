package org.jacorreu.embedding.application;

import org.jacorreu.embedding.application.dto.EmbeddingResult;
import org.jacorreu.embedding.application.dto.RetrieveEmbeddingCommand;
import org.jacorreu.embedding.core.domain.EmbeddingSearchResult;
import org.jacorreu.embedding.core.domain.valueobjects.EmbeddingUserId;
import org.jacorreu.embedding.core.domain.valueobjects.QueryText;
import org.jacorreu.embedding.core.domain.valueobjects.TopK;
import org.jacorreu.embedding.core.gateway.EmbeddingGateway;
import org.jacorreu.shared.validation.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetrieveEmbeddingUseCaseTest {

    @Mock
    private EmbeddingGateway embeddingGateway;

    @InjectMocks
    private RetrieveEmbeddingUseCase retrieveEmbeddingUseCase;

    @Test
    void execute_shouldSucceed_whenValidInput() {
        UUID userId = UUID.randomUUID();
        String query = "test query";
        int topK = 5;

        List<EmbeddingSearchResult> expectedResults = List.of(
                new EmbeddingSearchResult("content", Map.of("userId", userId.toString(), "type", "ONBOARDING"))
        );
        when(embeddingGateway.retrieve(any(EmbeddingUserId.class), any(QueryText.class), any(TopK.class)))
                .thenReturn(expectedResults);

        var command = new RetrieveEmbeddingCommand(userId, query, topK);
        var result = retrieveEmbeddingUseCase.execute(command);

        assertTrue(result.isSuccess());
        assertEquals(expectedResults, result.getData().results());
        verify(embeddingGateway).retrieve(any(EmbeddingUserId.class), any(QueryText.class), any(TopK.class));
    }

    @Test
    void execute_shouldReturnFailure_whenUserIdIsNull() {
        var command = new RetrieveEmbeddingCommand(null, "query", 5);
        var result = retrieveEmbeddingUseCase.execute(command);

        assertFalse(result.isSuccess());
        assertTrue(result.getNotification().getErrors().stream()
                .anyMatch(e -> e.field().equals("userId")));
        verify(embeddingGateway, never()).retrieve(any(), any(), any());
    }

    @Test
    void execute_shouldReturnFailure_whenQueryIsNull() {
        var command = new RetrieveEmbeddingCommand(UUID.randomUUID(), null, 5);
        var result = retrieveEmbeddingUseCase.execute(command);

        assertFalse(result.isSuccess());
        assertTrue(result.getNotification().getErrors().stream()
                .anyMatch(e -> e.field().equals("query")));
        verify(embeddingGateway, never()).retrieve(any(), any(), any());
    }

    @Test
    void execute_shouldReturnFailure_whenTopKIsZero() {
        var command = new RetrieveEmbeddingCommand(UUID.randomUUID(), "query", 0);
        var result = retrieveEmbeddingUseCase.execute(command);

        assertFalse(result.isSuccess());
        assertTrue(result.getNotification().getErrors().stream()
                .anyMatch(e -> e.field().equals("topK")));
        verify(embeddingGateway, never()).retrieve(any(), any(), any());
    }

    @Test
    void execute_shouldReturnFailure_whenTopKIsNegative() {
        var command = new RetrieveEmbeddingCommand(UUID.randomUUID(), "query", -1);
        var result = retrieveEmbeddingUseCase.execute(command);

        assertFalse(result.isSuccess());
        assertTrue(result.getNotification().getErrors().stream()
                .anyMatch(e -> e.field().equals("topK")));
        verify(embeddingGateway, never()).retrieve(any(), any(), any());
    }
}
