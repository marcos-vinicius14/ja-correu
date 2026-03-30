package org.jacorreu.embedding.application.dto;

import org.jacorreu.embedding.core.domain.EmbeddingSearchResult;

import java.util.List;

public record EmbeddingResult(List<EmbeddingSearchResult> results) {}
