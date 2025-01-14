package org.example.data.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class TimetableConfig {
    @JsonProperty("teachers")
    private List<TeacherConfig> teachers;

    @JsonProperty("subjects")
    private List<SubjectConfig> subjects;

    @JsonProperty("rooms")
    private RoomsConfig rooms;

    @JsonProperty("years")
    private Map<Integer, YearConfig> years;

    // Default constructor for Jackson
    public TimetableConfig() {
    }

    public void setRooms(RoomsConfig rooms) {
        this.rooms = rooms;
    }

    public Map<Integer, YearConfig> getYears() {
        return years;
    }

    public void setYears(Map<Integer, YearConfig> years) {
        this.years = years;
    }

    public List<TeacherConfig> getTeachers() {
        return teachers;
    }

    public List<SubjectConfig> getSubjects() {
        return subjects;
    }

    public RoomsConfig getRooms() {
        return rooms;
    }
}
