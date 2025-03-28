package com.DiscordEventCalendar;

import com.google.inject.Inject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.ArrayList;
import java.util.List;

public class SeshCalendarAPI {
    private final String apiUrl;
    private final OkHttpClient httpClient;

    @Inject
    public SeshCalendarAPI(OkHttpClient httpClient, String guildId, String startDate, String endDate) {
        this.httpClient = httpClient;
        this.apiUrl = "https://sesh.fyi/trpc/calendar.view?batch=1&input=%7B%220%22%3A%7B%22guild_id%22%3A%22"
                + guildId + "%22%2C%22range%22%3A%7B%22begin%22%3A%22"
                + startDate + "%22%2C%22end%22%3A%22" + endDate + "%22%7D%7D%7D";
    }

    private String sendRequest(String url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0")
                    .get()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.body() != null) {
                    return response.body().string();
                }
            }
        } catch (Exception e) {
            return "invalid request";
        }
        return "";
    }

    private List<DiscordEvent> parseEvents(String json) {
        List<DiscordEvent> events = new ArrayList<>();

        if (!json.contains("\"native_events\":[")) {
            return events;
        }

        String itemsData = extractSection(json, "\"native_events\":[");

        while (itemsData.contains("\"id\"")) {
            int eventStart = itemsData.indexOf("{");
            int eventEnd = itemsData.indexOf("}", eventStart);
            if (eventStart == -1 || eventEnd == -1) break;

            String eventBlock = itemsData.substring(eventStart, eventEnd);

            String name = extractValue(eventBlock, "\"name\":\"");
            String startTime = extractValue(eventBlock, "\"start_time_str\":\"");
            String endTime = extractValue(eventBlock, "\"end_time_str\":\"");
            String location = extractValue(eventBlock, "\"location\":\"");
            String description = extractValue(eventBlock, "\"description\":\"");

            events.add(new DiscordEvent(name, startTime, endTime, location, description));
            itemsData = itemsData.substring(eventEnd);
        }

        events.sort(DiscordEvent::compareTo);
        return events;
    }

    public List<DiscordEvent> fetchEvents() {
        String jsonResponse = sendRequest(apiUrl);
        return parseEvents(jsonResponse);
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
