package org.jacorreu.shared.validation;

public record Error(String field, String message) {
        public static Error of(String field, String message) {
            return new Error(field, message);
        }

        public static Error of(String message) {
            return new Error(null, message);
        }
}
