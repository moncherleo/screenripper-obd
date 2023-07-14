package utils;

public class TimeConverter {
    public static int convertToMilliseconds(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            throw new IllegalArgumentException("Invalid time format: " + timeString);
        }

        // Split the string into parts based on the delimiter ":"
        String[] parts = timeString.split(":");
        if (parts.length != 2 && parts.length != 3) {
            throw new IllegalArgumentException("Invalid time format: " + timeString);
        }

        // Initialize the variables
        int hours = 0;
        int minutes = 0;
        int seconds = 0;

        // Check the length of the parts array to determine the format of the time string
        if (parts.length == 2) {
            // Format: "minutes:seconds"
            minutes = Integer.parseInt(parts[0]);
            seconds = Integer.parseInt(parts[1]);
        } else {
            // Format: "hours:minutes:seconds"
            hours = Integer.parseInt(parts[0]);
            minutes = Integer.parseInt(parts[1]);
            seconds = Integer.parseInt(parts[2]);
        }

        // Convert hours, minutes, and seconds to milliseconds
        int hoursMillis = hours * 60 * 60 * 1000;
        int minutesMillis = minutes * 60 * 1000;
        int secondsMillis = seconds * 1000;

        // Calculate the total milliseconds
        return hoursMillis + minutesMillis + secondsMillis;
    }
}