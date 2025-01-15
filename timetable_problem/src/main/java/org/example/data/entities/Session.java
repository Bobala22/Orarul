package org.example.data.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Session {

    @JsonProperty("subject")
    private final Subject subject;
    @JsonProperty("isCourse")
    private final boolean isCourse;
    @JsonProperty("group")
    private final String group;
    @JsonProperty("teacher")
    private Teacher teacher;
    @JsonProperty("room")
    private Room room;
    @JsonProperty("timeSlot")
    private TimeSlot timeSlot;

    public Session(Subject subject, boolean isCourse, String group) {
        this.subject = subject;
        this.isCourse = isCourse;
        this.group = group;
        if (isCourse) {
            this.teacher = subject.getCourseTeacher();
        }
    }

    public Subject getSubject() {
        return subject;
    }

    public boolean isCourse() {
        return isCourse;
    }

    public String getGroup() {
        return group;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    @Override
    public String toString() {
        return subject.getName() + " (" + (isCourse ? "Course" : "Seminar") + ") - " + group;
    }
}