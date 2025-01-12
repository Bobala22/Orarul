package org.example.data.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.data.entities.TeacherRole;

public class TeacherConfig {


    @JsonProperty("name")
    private String name;

    @JsonProperty("role")
    private TeacherRole role;

    // Default constructor for Jackson
    public TeacherConfig() {
    }

    public String getName() {
        return name;
    }

    public TeacherRole getRole() {
        return role;
    }

}
