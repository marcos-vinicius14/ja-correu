package org.jacorreu.user.core.domain.valueobjects;

import org.jacorreu.shared.validation.Notification;
import org.jacorreu.shared.validation.Result;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class Password {
    private static final Pattern UPPERCASE = Pattern.compile(".*[A-Z].*");
    private static final Pattern LOWERCASE = Pattern.compile(".*[a-z].*");
    private static final Pattern DIGIT = Pattern.compile(".*[0-9].*");
    private static final Pattern SPECIAL = Pattern.compile(".*[@$!%*?&#].*");
    private static final Pattern WHITESPACE = Pattern.compile(".*\\s.*");

    private record Rule(Predicate<String> isValid, String errorMessage) { }

    private static final List<Rule> RULES = List.of(
            new Rule(s -> s.length() >= 8, "A senha deve ter pelo menos 8 caracteres."),
            new Rule(s -> s.length() <= 64, "A senha deve ter no máximo 64 caracteres."),
            new Rule(s -> UPPERCASE.matcher(s).matches(), "Deve conter pelo menos uma letra maiúscula."),
            new Rule(s -> LOWERCASE.matcher(s).matches(), "Deve conter pelo menos uma letra minúscula."),
            new Rule(s -> DIGIT.matcher(s).matches(), "Deve conter pelo menos um número."),
            new Rule(s -> SPECIAL.matcher(s).matches(), "Deve conter pelo menos um caractere especial."),
            new Rule(s -> !WHITESPACE.matcher(s).matches(), "Não pode conter espaços em branco.")
    );

    private final String value;

    private Password(String value) {
        this.value = value;
    }

    public static Result<Password> create(String value) {
        Notification notification = new Notification();

        if (value == null || value.isEmpty()) {
            notification.addError("senha", "A senha é obrigatória");
            return Result.failure(notification);
        }

        RULES.forEach(rule -> {
            if (!rule.isValid.test(value)) {
                notification.addError("senha", rule.errorMessage);
            }
        });

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        return Result.success(new Password(value));
    }

    public String getValue() {
        return value;
    }


}
