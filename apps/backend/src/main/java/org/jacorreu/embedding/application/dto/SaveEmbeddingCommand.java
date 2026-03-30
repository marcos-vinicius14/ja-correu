package org.jacorreu.embedding.application.dto;

import org.jacorreu.embedding.core.domain.EmbeddingType;

import java.util.UUID;

public record SaveEmbeddingCommand(UUID userId, EmbeddingType type, String content) {}