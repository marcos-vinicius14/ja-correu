package org.jacorreu.embedding.infra.adapter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.jacorreu.embedding.core.domain.EmbeddingSearchResult;
import org.jacorreu.embedding.core.domain.EmbeddingType;
import org.jacorreu.embedding.core.domain.valueobjects.EmbeddingContent;
import org.jacorreu.embedding.core.domain.valueobjects.EmbeddingUserId;
import org.jacorreu.embedding.core.domain.valueobjects.QueryText;
import org.jacorreu.embedding.core.domain.valueobjects.TopK;
import org.jacorreu.embedding.core.gateway.EmbeddingGateway;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class EmbeddingStorageAdapter implements EmbeddingGateway {

    private final VectorStore vectorStore;
    private final Counter saveSuccessCounter;
    private final Counter saveFailureCounter;
    private final Counter retrieveSuccessCounter;
    private final Counter retrieveFailureCounter;

    public EmbeddingStorageAdapter(VectorStore vectorStore, MeterRegistry meterRegistry) {
        this.vectorStore = vectorStore;
        this.saveSuccessCounter = Counter.builder("embedding_save_count")
                .description("Number of embedding save operations")
                .tag("operation", "save")
                .tag("status", "success")
                .register(meterRegistry);
        this.saveFailureCounter = Counter.builder("embedding_save_count")
                .description("Number of embedding save operations")
                .tag("operation", "save")
                .tag("status", "failure")
                .register(meterRegistry);
        this.retrieveSuccessCounter = Counter.builder("embedding_retrieve_count")
                .description("Number of embedding retrieve operations")
                .tag("operation", "retrieve")
                .tag("status", "success")
                .register(meterRegistry);
        this.retrieveFailureCounter = Counter.builder("embedding_retrieve_count")
                .description("Number of embedding retrieve operations")
                .tag("operation", "retrieve")
                .tag("status", "failure")
                .register(meterRegistry);
    }

    @Override
    public void save(EmbeddingUserId userId, EmbeddingType type, EmbeddingContent content) {
        try {
            var document = new Document(
                    content.getValue(),
                    Map.of(
                            "userId", userId.getValue().toString(),
                            "type", type.name()
                    )
            );

            vectorStore.add(List.of(document));
            saveSuccessCounter.increment();
        } catch (Exception e) {
            saveFailureCounter.increment();
            throw e;
        }
    }

    @Override
    public List<EmbeddingSearchResult> retrieve(EmbeddingUserId userId, QueryText query, TopK topK) {
        try {
            var filterExpression = "userId == '" + userId.getValue() + "'";

            var request = SearchRequest.builder()
                    .query(query.getValue())
                    .topK(topK.getValue())
                    .filterExpression(filterExpression)
                    .build();

            var documents = vectorStore.similaritySearch(request);
            var results = documents.stream()
                    .map(doc -> {
                        Map<String, String> stringMetadata = doc.getMetadata().entrySet().stream()
                                .collect(java.util.stream.Collectors.toMap(
                                        Map.Entry::getKey,
                                        e -> String.valueOf(e.getValue())
                                ));
                        return new EmbeddingSearchResult(doc.getText(), stringMetadata);
                    })
                    .toList();
            retrieveSuccessCounter.increment();
            return results;
        } catch (Exception e) {
            retrieveFailureCounter.increment();
            throw e;
        }
    }
}
