package com.DiscordEventCalendar;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class CalendarUtils {
    protected static String[] getCalendarStartAndEnd() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        // Get system's current date and time zone
        ZoneId zoneId = ZoneId.systemDefault(); // Gets the user's system timezone
        ZonedDateTime now = ZonedDateTime.now(zoneId);

        // Get the first and last day of the current month
        YearMonth currentMonth = YearMonth.from(now);
        LocalDate firstDay = currentMonth.atDay(1);
        LocalDate lastDay = currentMonth.atEndOfMonth();

        // Find the Sunday before or on the first day of the month
        LocalDate startOfCalendar = firstDay;
        while (startOfCalendar.getDayOfWeek() != DayOfWeek.SUNDAY) {
            startOfCalendar = startOfCalendar.minusDays(1);
        }

        // Find the Saturday after or on the last day of the month
        LocalDate endOfCalendar = lastDay;
        while (endOfCalendar.getDayOfWeek() != DayOfWeek.SATURDAY) {
            endOfCalendar = endOfCalendar.plusDays(1);
        }

        return new String[] {
                startOfCalendar.atStartOfDay(zoneId).format(formatter),
                endOfCalendar.atStartOfDay(zoneId).format(formatter)
        };
    }
}
