package com.DiscordEventCalendar;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DiscordEvent implements Comparable<DiscordEvent> {
    private final String name;
    private final String startTime;  // Formatted string
    private final String endTime;
    private final String location;
    private final String description;
    private final LocalDateTime startDateTime; // Unformatted for sorting

    public DiscordEvent(String name, String startTime, String endTime, String location,
                        String description) {
        this.name = name;
        this.startDateTime = parseToLocalDateTime(startTime); // Used for sorting
        this.startTime = formatEventTime(startTime); // Formatted for display
        this.endTime = formatEventTime(endTime);
        this.location = location;
        this.description = description;
    }

    public String getName() { return name; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }

    public LocalDateTime getStartDateTime() {
        return startDateTime; // Used for sorting
    }

    private String formatEventTime(String timeStr) {
        try {
            LocalDateTime localTime = parseToLocalDateTime(timeStr);
            if (localTime == LocalDateTime.MIN){
                throw new RuntimeException();
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy | hh:mm a");
            return localTime.format(formatter);
        } catch (Exception e) {
            System.err.println("Error formatting event time: " + e.getMessage());
            return "Unknown Time";
        }
    }

    private LocalDateTime parseToLocalDateTime(String timeStr) {
        try {
            Instant instant = Instant.parse(timeStr); // Convert to UTC Instant
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()); // Convert to local timezone
        } catch (Exception e) {
            System.err.println("Error parsing time: " + timeStr);
            return LocalDateTime.MIN; // Use earliest possible time if invalid
        }
    }

    @Override
    public int compareTo(DiscordEvent other) {
        return this.startDateTime.compareTo(other.startDateTime);
    }
}
