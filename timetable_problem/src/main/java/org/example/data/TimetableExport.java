package org.example.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// Add a DTO for JSON export
public class TimetableExport {
    @JsonProperty("schedule")
    private final List<ScheduleEntry> schedule;

    public TimetableExport(List<ScheduleEntry> schedule) {
        this.schedule = schedule;
    }

    public List<ScheduleEntry> getSchedule() {
        return schedule;
    }
}