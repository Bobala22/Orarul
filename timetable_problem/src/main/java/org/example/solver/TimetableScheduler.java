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
            return !teacherSchedule.get(teacher.getName()).contains(timeSlot);
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

        return backtrack(sessionsToSchedule, 0);
    }

    private boolean backtrack(List<Session> sessionsToSchedule, int index) {
        if (index == sessionsToSchedule.size()) {
            return true; // All sessions have been scheduled
        }

        Session session = sessionsToSchedule.get(index);
        List<Room> availableRooms = session.isCourse() ? courseRooms : seminarRooms;

        for (Room room : availableRooms) {
            for (String day : days) {
                for (Integer time : timeSlots) {
                    TimeSlot timeSlot = new TimeSlot(day, time);
                    if (isTimeSlotAvailable(room, session.getGroup(), session.getTeacher(), timeSlot)) {
                        session.setRoom(room);
                        session.setTimeSlot(timeSlot);
                        scheduleSession(session);

                        if (backtrack(sessionsToSchedule, index + 1)) {
                            return true;
                        }

                        // Undo the assignment
                        unscheduleSession(session);
                    }
                }
            }
        }

        return false; // No valid schedule found
    }

    private void unscheduleSession(Session session) {
        schedule.remove(session);

        // Update room schedule
        roomSchedule.get(session.getRoom().getName()).remove(session.getTimeSlot());

        // Update group schedule
        groupSchedule.get(session.getGroup()).remove(session.getTimeSlot());

        // Update teacher schedule
        teacherSchedule.get(session.getTeacher().getName()).remove(session.getTimeSlot());
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
        scheduleEntries.sort(Comparator.comparing((ScheduleEntry a) -> a.day).thenComparingInt(a -> a.startTime));

        // Create export object
        TimetableExport export = new TimetableExport(scheduleEntries);

        // Export to JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), export);
    }

    // Example usage
    public static void main(String[] args) {
        try {
            // Step 1: Load timetable configuration
            String configFilePath = "input.json"; // Path to your JSON config file
            TimetableConfigLoader configLoader = new TimetableConfigLoader();
            Map<Integer, TimetableData> timetableDataPerYear = configLoader.loadConfiguration(configFilePath);

            // Step 2: Create a scheduler sharing the same rooms (global rooms from the config)
            if (timetableDataPerYear.isEmpty()) {
                throw new IllegalStateException("No years found in configuration.");
            }

            // Assuming rooms are shared across years, get them from the first year's data
            TimetableData firstYearData = timetableDataPerYear.values().iterator().next(); // Get first year as a reference
            TimetableScheduler scheduler = new TimetableScheduler(
                    firstYearData.getCourseRooms(),
                    firstYearData.getSeminarRooms()
            );

            // Iterate through each year and generate timetables
            for (Map.Entry<Integer, TimetableData> entry : timetableDataPerYear.entrySet()) {
                System.out.println("\nProcessing timetable for Year " + entry.getKey());
                TimetableData yearData = entry.getValue();

                // Step 3: Generate timetable for the year
                if (scheduler.generateTimetable(yearData.getSubjects())) {
                    System.out.println("Successfully generated timetable for Year " + entry.getKey());
                    scheduler.printSchedule();

                    String outputPath = "timetable_year_" + entry.getKey() + ".json";
                    scheduler.exportToJson(outputPath);
                    System.out.println("Exported timetable to " + outputPath);
                } else {
                    System.out.println("Failed to generate a valid timetable for Year " + entry.getKey());
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading or writing JSON configuration: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("Configuration error: " + e.getMessage());
        }
    }

    public List<Session> getSchedule() {
        return schedule;
    }
}
