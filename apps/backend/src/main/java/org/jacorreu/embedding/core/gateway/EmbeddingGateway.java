package org.jacorreu.embedding.core.gateway;

import org.jacorreu.embedding.core.domain.EmbeddingSearchResult;
import org.jacorreu.embedding.core.domain.EmbeddingType;
import org.jacorreu.embedding.core.domain.valueobjects.EmbeddingContent;
import org.jacorreu.embedding.core.domain.valueobjects.EmbeddingUserId;
import org.jacorreu.embedding.core.domain.valueobjects.QueryText;
import org.jacorreu.embedding.core.domain.valueobjects.TopK;

import java.util.List;

public interface EmbeddingGateway {
    void save(EmbeddingUserId userId, EmbeddingType type, EmbeddingContent content);

    List<EmbeddingSearchResult> retrieve(EmbeddingUserId userId, QueryText query, TopK topK);
}
