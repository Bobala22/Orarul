package org.example.data.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class YearConfig {
    @JsonProperty("teachers") // List of teachers for this year
    private List<TeacherConfig> teachers;

    @JsonProperty("subjects") // Subjects specific to this year
    private List<SubjectConfig> subjects;

    @JsonProperty("series") // Series of students, e.g., A, B, etc.
    private List<String> series;

    @JsonProperty("groups") // Groups within each series (e.g., A1, A2, etc.)
    private Map<String, Integer> groups;

    // Getters and Setters
    public List<TeacherConfig> getTeachers() {
        return teachers;
    }

    public void setTeachers(List<TeacherConfig> teachers) {
        this.teachers = teachers;
    }

    public List<SubjectConfig> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<SubjectConfig> subjects) {
        this.subjects = subjects;
    }

    public List<String> getSeries() {
        return series;
    }

    public void setSeries(List<String> series) {
        this.series = series;
    }

    public Map<String, Integer> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, Integer> groups) {
        this.groups = groups;
    }
}
