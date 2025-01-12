package org.example.data;

import org.example.data.entities.Room;
import org.example.data.entities.Subject;

import java.util.List;

// Class to hold configuration data
public class TimetableData {
    private final List<Subject> subjects;
    private final List<Room> courseRooms;
    private final List<Room> seminarRooms;

    public TimetableData(List<Subject> subjects, List<Room> courseRooms, List<Room> seminarRooms) {
        this.subjects = subjects;
        this.courseRooms = courseRooms;
        this.seminarRooms = seminarRooms;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public List<Room> getCourseRooms() {
        return courseRooms;
    }

    public List<Room> getSeminarRooms() {
        return seminarRooms;
    }
}
