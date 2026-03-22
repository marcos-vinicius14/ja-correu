package org.jacorreu.user.core.domain.valueobjects;

import org.jacorreu.shared.validation.Notification;
import org.jacorreu.shared.validation.Result;

import java.util.function.Consumer;
import java.util.function.Function;

public sealed interface StravaToken permits ValidStravaToken, EmptyStravaToken {
    boolean isLinked();
    boolean isExpired();
    void execute(Consumer<String> action);

}
