package org.jacorreu.embedding.application.dto;

import org.springframework.ai.document.Document;

import java.util.List;

public record EmbeddingResult(List<Document> documents) {}