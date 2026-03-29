package org.jacorreu.onboarding.core.domain.valueobjects;

import org.jacorreu.shared.validation.Notification;
import org.jacorreu.shared.validation.Result;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CurrentPacePerKm {

    private static final int MIN_SECONDS = 180;  // 3:00 min/km
    private static final int MAX_SECONDS = 900;  // 15:00 min/km
    private static final Pattern PACE_PATTERN = Pattern.compile("^(\\d{1,2}):(\\d{2})$");

    private final int secondsPerKm;

    private CurrentPacePerKm(int secondsPerKm) {
        this.secondsPerKm = secondsPerKm;
    }

    public static Result<CurrentPacePerKm> create(String paceStr) {
        Notification notification = new Notification();

        if (paceStr == null || paceStr.isBlank()) {
            notification.addError("currentPacePerKm", "O pace não pode ser vazio");
            return Result.failure(notification);
        }

        Matcher matcher = PACE_PATTERN.matcher(paceStr.trim());
        if (!matcher.matches()) {
            notification.addError("currentPacePerKm", "Formato de pace inválido. Use MM:SS (ex: 06:30)");
            return Result.failure(notification);
        }

        int minutes = Integer.parseInt(matcher.group(1));
        int seconds = Integer.parseInt(matcher.group(2));

        if (seconds >= 60) {
            notification.addError("currentPacePerKm", "Segundos devem ser entre 00 e 59");
            return Result.failure(notification);
        }

        int totalSeconds = minutes * 60 + seconds;

        if (totalSeconds < MIN_SECONDS || totalSeconds > MAX_SECONDS) {
            notification.addError("currentPacePerKm",
                    "Pace deve ser entre 03:00 e 15:00 min/km");
            return Result.failure(notification);
        }

        return Result.success(new CurrentPacePerKm(totalSeconds));
    }

    public static CurrentPacePerKm restore(int secondsPerKm) {
        return new CurrentPacePerKm(secondsPerKm);
    }

    public int toSeconds() {
        return secondsPerKm;
    }

    public String toFormattedString() {
        int minutes = secondsPerKm / 60;
        int seconds = secondsPerKm % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
