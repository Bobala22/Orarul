package org.example.solver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.data.*;
import org.example.data.config.TimetableConfigLoader;
import org.example.data.entities.*;

import java.io.File;
import java.io.IOException;
import java.util.*;


// Update data classes to support JSON serialization


public class TimetableScheduler {
    private final List<String> days;
    private final List<Integer> timeSlots;
    private final List<String> series;
    private final Map<String, Integer> groupsPerSeries;
    private final List<Room> courseRooms;
    private final List<Room> seminarRooms;

    private final List<Session> schedule;
    private final Map<String, Set<TimeSlot>> roomSchedule;
    private final Map<String, Set<TimeSlot>> groupSchedule;
    private final Map<String, Set<TimeSlot>> teacherSchedule;

    public TimetableScheduler(List<Room> courseRooms, List<Room> seminarRooms) {
        this.days = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday");
        this.timeSlots = new ArrayList<>();
        for (int i = 8; i < 20; i += 2) {
            timeSlots.add(i);
        }
        this.series = Arrays.asList("A", "B", "E");

        this.groupsPerSeries = new HashMap<>();
        groupsPerSeries.put("A", 5);
        groupsPerSeries.put("B", 4);
        groupsPerSeries.put("E", 3);

        this.courseRooms = new ArrayList<>(courseRooms);
        this.seminarRooms = new ArrayList<>(seminarRooms);

        this.schedule = new ArrayList<>();
        this.roomSchedule = new HashMap<>();
        this.groupSchedule = new HashMap<>();
        this.teacherSchedule = new HashMap<>();
    }
    private boolean isTimeSlotAvailable(Room room, String group, Teacher teacher, TimeSlot timeSlot) {
        // Check if room is available
        if (roomSchedule.containsKey(room.getName())) {
            if (roomSchedule.get(room.getName()).contains(timeSlot)) {
                return false;
            }
        }

        // Check if group is available
        if (groupSchedule.containsKey(group)) {
            if (groupSchedule.get(group).contains(timeSlot)) {
                return false;
            }
        }

        // Check if teacher is available
        if (teacherSchedule.containsKey(teacher.getName())) {
            if (teacherSchedule.get(teacher.getName()).contains(timeSlot)) {
                return false;
            }
        }

        return true;
    }

    public boolean generateTimetable(List<Subject> subjects) {
        List<Session> sessionsToSchedule = new ArrayList<>();

        // Create all required sessions
        for (Subject subject : subjects) {
            // Create course sessions (one per series)
            for (String series : this.series) {
                sessionsToSchedule.add(new Session(subject, true, series));
            }

            // Create seminar sessions (one per group)
            for (String series : this.series) {
                for (int i = 1; i <= groupsPerSeries.get(series); i++) {
                    Session seminarSession = new Session(subject, false, series + i);
                    // Randomly assign one of the seminar teachers
                    List<Teacher> availableTeachers = subject.getSeminarTeachers();
                    seminarSession.setTeacher(
                            availableTeachers.get(new Random().nextInt(availableTeachers.size()))
                    );
                    sessionsToSchedule.add(seminarSession);
                }
            }
        }

        // Try to schedule all sessions
        for (Session session : sessionsToSchedule) {
            boolean scheduled = false;
            List<Room> availableRooms = session.isCourse() ? courseRooms : seminarRooms;

            // Try all possible combinations of rooms and time slots
            for (Room room : availableRooms) {
                if (scheduled) break;

                for (String day : days) {
                    if (scheduled) break;

                    for (Integer time : timeSlots) {
                        TimeSlot timeSlot = new TimeSlot(day, time);
                        if (isTimeSlotAvailable(room, session.getGroup(),
                                session.getTeacher(), timeSlot)) {

                            session.setRoom(room);
                            session.setTimeSlot(timeSlot);
                            scheduleSession(session);
                            scheduled = true;
                            break;
                        }
                    }
                }
            }

            if (!scheduled) {
                return false; // Could not schedule all sessions
            }
        }

        return true;
    }
    private void scheduleSession(Session session) {
        schedule.add(session);

        // Update room schedule
        roomSchedule.computeIfAbsent(session.getRoom().getName(), k -> new HashSet<>())
                   .add(session.getTimeSlot());

        // Update group schedule
        groupSchedule.computeIfAbsent(session.getGroup(), k -> new HashSet<>())
                    .add(session.getTimeSlot());

        // Update teacher schedule
        teacherSchedule.computeIfAbsent(session.getSubject().getTeacher(), k -> new HashSet<>())
                      .add(session.getTimeSlot());
    }

    public void printSchedule() {
        // Sort schedule by day, time, and group for better readability
        schedule.sort((a, b) -> {
            int dayCompare = a.getTimeSlot().getDay().compareTo(b.getTimeSlot().getDay());
            if (dayCompare != 0) return dayCompare;

            int timeCompare = Integer.compare(a.getTimeSlot().getStartTime(),
                                            b.getTimeSlot().getStartTime());
            if (timeCompare != 0) return timeCompare;

            return a.getGroup().compareTo(b.getGroup());
        });

        for (Session session : schedule) {
            System.out.printf("%s, %02d:00 - %02d:00, %s, %s, %s, %s, Room: %s%n",
                session.getTimeSlot().getDay(),
                session.getTimeSlot().getStartTime(),
                session.getTimeSlot().getStartTime() + 2,
                session.getSubject().getName(),
                session.isCourse() ? "Course" : "Seminar",
                session.getGroup(),
                session.getSubject().getTeacher(),
                session.getRoom().getName());
        }
    }

    public void exportToJson(String filePath) throws IOException {
        // Convert sessions to schedule entries
        List<ScheduleEntry> scheduleEntries = new ArrayList<>();
        for (Session session : schedule) {
            scheduleEntries.add(new ScheduleEntry(session));
        }

        // Sort schedule entries
        scheduleEntries.sort((a, b) -> {
            int dayCompare = a.day.compareTo(b.day);
            if (dayCompare != 0) return dayCompare;
            return Integer.compare(a.startTime, b.startTime);
        });

        // Create export object
        TimetableExport export = new TimetableExport(scheduleEntries);

        // Export to JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), export);
    }

    // Example usage
    public static void main(String[] args) {
        try {
            // Load configuration from JSON file
            TimetableConfigLoader configLoader = new TimetableConfigLoader();
            TimetableData data = configLoader.loadConfiguration("input.json");

            // Create scheduler with configured rooms
            TimetableScheduler scheduler = new TimetableScheduler(
                    data.getCourseRooms(),
                    data.getSeminarRooms()
            );

            // Generate timetable
            if (scheduler.generateTimetable(data.getSubjects())) {
                System.out.println("Successfully generated timetable:");
                scheduler.printSchedule();

                scheduler.exportToJson("timetable_output.json");
                System.out.println("\nTimetable exported to timetable_output.json");
            } else {
                System.out.println("Failed to generate a valid timetable.");
            }
        } catch (IOException e) {
            System.err.println("Error reading/writing JSON file: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Configuration error: " + e.getMessage());
        }
    }
}
