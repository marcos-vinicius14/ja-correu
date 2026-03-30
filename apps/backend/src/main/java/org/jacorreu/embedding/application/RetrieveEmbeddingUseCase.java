package org.jacorreu.embedding.application;

import org.jacorreu.embedding.application.dto.EmbeddingResult;
import org.jacorreu.embedding.application.dto.RetrieveEmbeddingCommand;
import org.jacorreu.embedding.core.domain.valueobjects.EmbeddingUserId;
import org.jacorreu.embedding.core.domain.valueobjects.QueryText;
import org.jacorreu.embedding.core.domain.valueobjects.TopK;
import org.jacorreu.embedding.core.gateway.EmbeddingGateway;
import org.jacorreu.shared.validation.Notification;
import org.jacorreu.shared.validation.Result;

public class RetrieveEmbeddingUseCase {

    private final EmbeddingGateway embeddingGateway;

    public RetrieveEmbeddingUseCase(EmbeddingGateway embeddingGateway) {
        this.embeddingGateway = embeddingGateway;
    }

    public Result<EmbeddingResult> execute(RetrieveEmbeddingCommand command) {
        var notification = new Notification();

        var userId = EmbeddingUserId.create(command.userId());
        var query = QueryText.create(command.query());
        var topK = TopK.create(command.topK());

        notification.merge(userId);
        notification.merge(query);
        notification.merge(topK);

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        var results = embeddingGateway.retrieve(userId.getData(), query.getData(), topK.getData());
        return Result.success(new EmbeddingResult(results));
    }
}
