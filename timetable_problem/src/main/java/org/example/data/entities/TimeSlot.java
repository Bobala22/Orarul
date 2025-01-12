package org.example.data.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class TimeSlot {
    @JsonProperty("day")
    private final String day;
    @JsonProperty("startTime")
    private final int startTime;

    public TimeSlot(String day, int startTime) {
        this.day = day;
        this.startTime = startTime;
    }

    public String getDay() {
        return day;
    }

    public int getStartTime() {
        return startTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeSlot)) return false;
        TimeSlot other = (TimeSlot) o;
        return startTime == other.startTime && day.equals(other.day);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, startTime);
    }
}
