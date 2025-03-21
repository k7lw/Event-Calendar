package com.DiscordEventCalendar;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SeshCalendarAPI {
    private final String apiUrl;

    public SeshCalendarAPI(String guildId, String startDate, String endDate) {
        this.apiUrl = "https://sesh.fyi/trpc/calendar.view?batch=1&input=%7B%220%22%3A%7B%22guild_id%22%3A%22"
                + guildId + "%22%2C%22range%22%3A%7B%22begin%22%3A%22"
                + startDate + "%22%2C%22end%22%3A%22" + endDate + "%22%7D%7D%7D";
    }

    private String sendRequest(String url) {
        StringBuilder response = new StringBuilder();
        HttpURLConnection connection = null;
        try {
            URL requestUrl = new URL(url);
            connection = (HttpURLConnection) requestUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
        } catch (Exception e) {

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return response.toString();
    }

    private List<DiscordEvent> parseEvents(String json) {
        List<DiscordEvent> events = new ArrayList<>();

        if (!json.contains("\"native_events\":[")) {
            return events;
        }

        // Extract event list inside `"items":[ ... ]`
        String itemsData = extractSection(json, "\"native_events\":[");

        // Iterate through multiple events inside `"items"`
        while (itemsData.contains("\"id\"")) {
            int eventStart = itemsData.indexOf("{"); // Start of an event block
            int eventEnd = itemsData.indexOf("}", eventStart); // Find the end of the event block

            if (eventStart == -1 || eventEnd == -1) break; // Stop if no more events

            // Extract the full event JSON block
            String eventBlock = itemsData.substring(eventStart, eventEnd);

            // Extract individual fields safely
            String name = extractValue(eventBlock, "\"name\":\"");
            String startTime = extractValue(eventBlock, "\"start_time_str\":\"");
            String endTime = extractValue(eventBlock, "\"end_time_str\":\"");
            String location = extractValue(eventBlock, "\"location\":\"");
            String description = extractValue(eventBlock, "\"description\":\"");

            // Create and add the event
            events.add(new DiscordEvent(name, startTime, endTime, location, description));

            // Move to the next event in the list
            itemsData = itemsData.substring(eventEnd);
        }

        return events;
    }

    public List<DiscordEvent> fetchEvents() {
        String jsonResponse = sendRequest(apiUrl);
        List<DiscordEvent> events = parseEvents(jsonResponse);
        // Sort events by start time
        events.sort(DiscordEvent::compareTo);

        return events;
    }



    private String extractValue(String json, String key) {
        if (!json.contains(key)) return "";
        String sub = json.substring(json.indexOf(key) + key.length());
        return sub.substring(0, sub.indexOf("\""));
    }

    private String extractSection(String json, String key) {
        if (!json.contains(key)) return "";
        String sub = json.substring(json.indexOf(key) + key.length());
        return sub.substring(0, sub.indexOf("]") + 1);
    }
}
