package org.jacorreu.embedding.core.domain;

import java.util.Map;
import java.util.UUID;

public record EmbeddingSearchResult(
        String content,
        Map<String, String> metadata
) {
    public UUID userId() {
        return UUID.fromString(metadata.get("userId"));
    }

    public String type() {
        return metadata.get("type");
    }
}
