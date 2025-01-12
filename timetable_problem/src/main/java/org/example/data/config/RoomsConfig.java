package org.example.data.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class RoomsConfig {
    @JsonProperty("courseRooms")
    private List<String> courseRooms;

    @JsonProperty("seminarRooms")
    private List<String> seminarRooms;

    public RoomsConfig() {
    }

    public List<String> getCourseRooms() {
        return courseRooms;
    }

    public List<String> getSeminarRooms() {
        return seminarRooms;
    }
}