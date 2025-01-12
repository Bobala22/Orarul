package org.example.data.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SubjectConfig {

    @JsonProperty("name")
    private String name;

    @JsonProperty("courseTeacher")
    private String courseTeacherName;

    @JsonProperty("seminarTeachers")
    private List<String> seminarTeacherNames;

    // Default constructor for Jackson
    public SubjectConfig() {
    }

    public String getName() {
        return name;
    }

    public String getCourseTeacherName() {
        return courseTeacherName;
    }

    public List<String> getSeminarTeacherNames() {
        return seminarTeacherNames;
    }

}
