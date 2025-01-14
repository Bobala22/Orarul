package org.example.data.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SubjectConfig {

    @JsonProperty("name")
    private String name;

    @JsonProperty("courseTeacher")
    private String courseTeacher;

    @JsonProperty("seminarTeacher")
    private List<String> seminarTeacher;

    // Default constructor for Jackson
    public SubjectConfig() {
    }

    public String getName() {
        return name;
    }

    public String getCourseTeacher() {
        return courseTeacher;
    }

    public List<String> getSeminarTeacher() {
        return seminarTeacher;
    }

}
