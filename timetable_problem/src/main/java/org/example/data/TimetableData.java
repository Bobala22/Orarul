package org.example.data;

import org.example.data.entities.Room;
import org.example.data.entities.Subject;

import java.util.List;
import java.util.Map;

// Class to hold configuration data
public class TimetableData {
    private  List<Subject> subjects;
    private  List<Room> courseRooms;
    private  List<Room> seminarRooms;
    private List<String> series; // Add series
    private Map<String, Integer> groupsPerSeries; // Add groupsPerSeries

    // Getters and setters for courseRooms
    public List<Room> getCourseRooms() {
        return courseRooms;
    }

    public void setCourseRooms(List<Room> courseRooms) {
        this.courseRooms = courseRooms;
    }

    // Getters and setters for seminarRooms
    public List<Room> getSeminarRooms() {
        return seminarRooms;
    }

    public void setSeminarRooms(List<Room> seminarRooms) {
        this.seminarRooms = seminarRooms;
    }

    // Getters and setters for subjects
    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<Subject> subjects) {
        this.subjects = subjects;
    }

    // Getters and setters for series
    public List<String> getSeries() {
        return series;
    }

    public void setSeries(List<String> series) {
        this.series = series;
    }

    // Getters and setters for groupsPerSeries
    public Map<String, Integer> getGroupsPerSeries() {
        return groupsPerSeries;
    }

    public void setGroupsPerSeries(Map<String, Integer> groupsPerSeries) {
        this.groupsPerSeries = groupsPerSeries;
    }

    public TimetableData(List<Subject> subjects, List<Room> courseRooms, List<Room> seminarRooms) {
        this.subjects = subjects;
        this.courseRooms = courseRooms;
        this.seminarRooms = seminarRooms;
    }
}
