package org.jacorreu.embedding.application;

import org.jacorreu.embedding.application.dto.SaveEmbeddingCommand;
import org.jacorreu.embedding.core.domain.EmbeddingType;
import org.jacorreu.embedding.core.domain.valueobjects.EmbeddingContent;
import org.jacorreu.embedding.core.domain.valueobjects.EmbeddingUserId;
import org.jacorreu.embedding.core.gateway.EmbeddingGateway;
import org.jacorreu.shared.validation.Notification;
import org.jacorreu.shared.validation.Result;

public class SaveEmbeddingUseCase {

    private final EmbeddingGateway embeddingGateway;

    public SaveEmbeddingUseCase(EmbeddingGateway embeddingGateway) {
        this.embeddingGateway = embeddingGateway;
    }

    public Result<Void> execute(SaveEmbeddingCommand command) {
        var notification = new Notification();

        var userId = EmbeddingUserId.create(command.userId());
        var content = EmbeddingContent.create(command.content());

        notification.merge(userId);
        notification.merge(content);

        if (command.type() == null) {
            notification.addError("type", "tipo não pode ser nulo");
        }

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        embeddingGateway.save(userId.getData(), command.type(), content.getData());
        return Result.success();
    }
}
