package org.example.data.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import org.example.data.entities.Room;
import org.example.data.entities.Teacher;

public class TimetableConfig {
    
    @JsonProperty("teachers")
    private List<TeacherConfig> teachers;

    @JsonProperty("subjects")
    private List<SubjectConfig> subjects;

    @JsonProperty("rooms")
    private RoomsConfig rooms;

    // Default constructor for Jackson
    public TimetableConfig() {
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
