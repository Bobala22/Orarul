package org.example.data.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class TimeSlot {
    @JsonProperty("day")
    private final String day;
    @JsonProperty("startTime")
    private final int startTime;
    @JsonProperty("endTime")
    private final int endTime; // Adding endTime field


    public TimeSlot(String day, int startTime) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = startTime + 2; // Default to 2 hours
    }

    public TimeSlot(String day, int startTime, int endTime) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getDay() {
        return day;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    // Check if this time slot overlaps with another
    public boolean overlaps(TimeSlot other) {
        if (!this.day.equals(other.day)) {
            return false;
        }
        // Check if one slot's start time falls within the other's range
        return (this.startTime < other.endTime && other.startTime < this.endTime);
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
