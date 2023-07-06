package utils;

public class TimeConverter {
    public static int convertToMilliseconds(String timeString) {
        // Split the string into minutes and seconds
        String[] parts = timeString.split(":");
        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);

        // Convert minutes to milliseconds
        int minutesMillis = minutes * 60 * 1000;

        // Convert seconds to milliseconds
        int secondsMillis = seconds * 1000;

        // Calculate the total milliseconds
        int totalMillis = minutesMillis + secondsMillis;

        return totalMillis;
    }
}