package org.example.data.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.data.entities.TeacherRole;
import org.example.data.entities.TimeSlot;

import java.util.ArrayList;
import java.util.List;

public class TeacherConfig {
    @JsonProperty("name")
    private String name;

    @JsonProperty("role")
    private TeacherRole role;

    @JsonProperty("maxHoursPerWeek")
    private int maxHoursPerWeek;

    @JsonProperty("unavailableTimes")
    private List<String> unavailableTimes = new ArrayList<>(); // Default to empty list if not specified

    // Default constructor for Jackson
    public TeacherConfig() {
    }

    public String getName() {
        return name;
    }

    public TeacherRole getRole() {
        return role;
    }

    public int getMaxHours() {
        return maxHoursPerWeek;
    }

    public List<String> getUnavailableTimes() {
        return unavailableTimes;
    }

    // Helper method to parse unavailable times into TimeSlot objects
    public List<TimeSlot> getUnavailableTimeSlots() {
        List<TimeSlot> slots = new ArrayList<>();
        for (String timeStr : unavailableTimes) {
            String[] parts = timeStr.split(" ");
            String day = parts[0];
            String[] hours = parts[1].split("-");
            int startHour = Integer.parseInt(hours[0]);
            int endHour = Integer.parseInt(hours[1]);
            slots.add(new TimeSlot(day, startHour, endHour)); // Using new constructor with end time
        }
        return slots;
    }
}