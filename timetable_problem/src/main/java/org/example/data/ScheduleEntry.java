package org.example.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.data.entities.Session;
import org.example.data.entities.TeacherRole;

public class ScheduleEntry {

    @JsonProperty("day")
    public final String day;
    @JsonProperty("startTime")
    public final int startTime;
    @JsonProperty("endTime")
    private final int endTime;
    @JsonProperty("subject")
    private final String subject;
    @JsonProperty("type")
    private final String type;
    @JsonProperty("group")
    private final String group;
    @JsonProperty("teacher")
    private final String teacher;
    @JsonProperty("teacherRole")
    private final TeacherRole teacherRole;
    @JsonProperty("room")
    private final String room;


    public ScheduleEntry( Session session) {
        this.day = session.getTimeSlot().getDay();
        this.startTime = session.getTimeSlot().getStartTime();
        this.endTime = session.getTimeSlot().getStartTime() + 2;
        this.subject = session.getSubject().getName();
        this.type = session.isCourse() ? "Course" : "Seminar";
        this.group = session.getGroup();
        this.teacher = session.getTeacher().getName();
        this.teacherRole = session.getTeacher().getRole();
        this.room = session.getRoom().getName();
    }

    @Override
    public String toString() {
        return "ScheduleEntry{" +
                "day='" + day + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", subject='" + subject + '\'' +
                ", type='" + type + '\'' +
                ", group='" + group + '\'' +
                ", teacher='" + teacher + '\'' +
                ", teacherRole=" + teacherRole +
                ", room='" + room + '\'' +
                '}';
    }

}
