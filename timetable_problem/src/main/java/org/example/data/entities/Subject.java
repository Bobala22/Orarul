package org.example.data.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Subject {


    @JsonProperty("name")
    private final String name;
    @JsonProperty("courseTeacher")
    private final Teacher courseTeacher;
    @JsonProperty("seminarTeachers")
    private final List<Teacher> seminarTeachers;

    public Subject(String name, Teacher courseTeacher, List<Teacher> seminarTeachers) {
        if (courseTeacher != null && !courseTeacher.canTeachCourse()) {
            throw new IllegalArgumentException("Course teacher must be a lecturer");
        }
        this.name = name;
        this.courseTeacher = courseTeacher;
        this.seminarTeachers = new ArrayList<>(seminarTeachers);
    }

    public String getName() {
        return name;
    }

    public Teacher getCourseTeacher() {
        return courseTeacher;
    }

    public List<Teacher> getSeminarTeachers() {
        return Collections.unmodifiableList(seminarTeachers);
    }

    public String getTeacher() {
        return courseTeacher.getName();
    }
}
