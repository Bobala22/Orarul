package org.example.data.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Teacher {


    @JsonProperty("name")
    private final String name;
    @JsonProperty("role")
    private final TeacherRole role;
    public Teacher(String name, TeacherRole role) {
        this.name = name;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public TeacherRole getRole() {
        return role;
    }

    public boolean canTeachCourse() {
        return role == TeacherRole.LECTURER;
    }

}
