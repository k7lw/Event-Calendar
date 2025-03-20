package com.DiscordEventCalendar;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

public class CalendarUtils {

    private static String startDateTime;
    private static String endDateTime;

    public CalendarUtils() {
        getCalendarStartAndEnd();
    }
    public static String get_startDateTime(){
        return startDateTime;
    }
    public static String get_endDateTime(){
        return endDateTime;
    }
    private static void getCalendarStartAndEnd() {
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

        // Convert to OffsetDateTime with UTC-08:00 offset (Pacific Time Standard)
        ZonedDateTime local_startDateTime = startOfCalendar.atStartOfDay(zoneId);
        ZonedDateTime local_endDateTime = endOfCalendar.atStartOfDay(zoneId);

        startDateTime = local_startDateTime.format(formatter);
        endDateTime = local_endDateTime.format(formatter);
    }
}
