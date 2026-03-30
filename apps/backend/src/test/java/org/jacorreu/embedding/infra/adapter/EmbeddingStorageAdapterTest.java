package org.jacorreu.embedding.infra.adapter;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.jacorreu.embedding.core.domain.EmbeddingSearchResult;
import org.jacorreu.embedding.core.domain.EmbeddingType;
import org.jacorreu.embedding.core.domain.valueobjects.EmbeddingContent;
import org.jacorreu.embedding.core.domain.valueobjects.EmbeddingUserId;
import org.jacorreu.embedding.core.domain.valueobjects.QueryText;
import org.jacorreu.embedding.core.domain.valueobjects.TopK;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmbeddingStorageAdapterTest {

    @Mock
    private VectorStore vectorStore;

    private SimpleMeterRegistry meterRegistry;
    private EmbeddingStorageAdapter embeddingAdapter;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        embeddingAdapter = new EmbeddingStorageAdapter(vectorStore, meterRegistry);
    }

    @Test
    void save_shouldCreateDocumentWithCorrectMetadata_whenValidInput() {
        UUID userId = UUID.randomUUID();
        EmbeddingType type = EmbeddingType.ONBOARDING;
        String contentValue = "test content";

        var userIdVO = EmbeddingUserId.restore(userId);
        var contentVO = EmbeddingContent.restore(contentValue);

        embeddingAdapter.save(userIdVO, type, contentVO);

        ArgumentCaptor<List<Document>> docCaptor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(docCaptor.capture());

        Document savedDoc = docCaptor.getValue().get(0);
        assertEquals(contentValue, savedDoc.getText());
        assertEquals(userId.toString(), savedDoc.getMetadata().get("userId"));
        assertEquals(type.name(), savedDoc.getMetadata().get("type"));
    }

    @Test
    void retrieve_shouldCreateSearchRequestWithUserIdFilter_whenValidInput() {
        UUID userId = UUID.randomUUID();
        String queryValue = "test query";
        int topKValue = 5;

        var userIdVO = EmbeddingUserId.restore(userId);
        var queryVO = QueryText.restore(queryValue);
        var topKVO = TopK.restore(topKValue);

        Document doc = new Document("result content", Map.of("userId", userId.toString(), "type", "ONBOARDING"));
        List<Document> documents = List.of(doc);
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(documents);

        var results = embeddingAdapter.retrieve(userIdVO, queryVO, topKVO);

        assertEquals(1, results.size());
        assertEquals("result content", results.get(0).content());
        assertEquals(userId.toString(), results.get(0).metadata().get("userId"));
        assertEquals("ONBOARDING", results.get(0).metadata().get("type"));

        ArgumentCaptor<SearchRequest> requestCaptor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(requestCaptor.capture());

        SearchRequest request = requestCaptor.getValue();
        assertEquals(queryValue, request.getQuery());
        assertEquals(topKValue, request.getTopK());
        assertNotNull(request.getFilterExpression());
        String filterExpression = request.getFilterExpression().toString();
        assertTrue(filterExpression.contains(userId.toString()));
    }
}
