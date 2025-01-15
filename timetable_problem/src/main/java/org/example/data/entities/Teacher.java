package org.example.data.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Teacher {


    @JsonProperty("name")
    private final String name;
    @JsonProperty("role")
    private final TeacherRole role;
    @JsonProperty("maxHoursPerWeek")
    private final int maxHoursPerWeek;
    @JsonProperty("unavailableTimes")
    private final List<TimeSlot> unavailableTimes;


    public Teacher(String name, TeacherRole role, int maxHoursPerWeek, List<TimeSlot> unavailableTimeSlots) {
        this.name = name;
        this.role = role;
        this.maxHoursPerWeek = maxHoursPerWeek;
        this.unavailableTimes = unavailableTimeSlots != null ? unavailableTimeSlots : new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public TeacherRole getRole() {
        return role;
    }

    public int getMaxHoursPerWeek() {
        return maxHoursPerWeek;
    }

    public List<TimeSlot> getUnavailableTimeSlots() {
        return unavailableTimes;
    }

    public boolean isAvailableAt(TimeSlot timeSlot) {
        return unavailableTimes.stream()
                .noneMatch(unavailable -> unavailable.overlaps(timeSlot));
    }

    public boolean canTeachCourse() {
        return role == TeacherRole.LECTURER;
    }
}
