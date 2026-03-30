package org.jacorreu.embedding.application.dto;

import java.util.UUID;

public record RetrieveEmbeddingCommand(UUID userId, String query, int topK) {}