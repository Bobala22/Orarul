package org.example.data.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Teacher {


    @JsonProperty("name")
    private final String name;
    @JsonProperty("role")
    private final TeacherRole role;
    @JsonProperty("maxHoursPerWeek")
    private final int maxHoursPerWeek;

    public Teacher(String name, TeacherRole role,int maxHoursPerWeek) {
        this.name = name;
        this.role = role;
        this.maxHoursPerWeek=maxHoursPerWeek;
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

    public int getMaxHoursPerWeek() {
        return maxHoursPerWeek;
    }
}
