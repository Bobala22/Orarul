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
    private Map<Teacher, Integer> teacherHours;
    private long startTime;
    private static final long TIMEOUT = 10000; // 10 seconds

    private static final double UNAVAILABLE_TIME_PENALTY = 0.8; // Penalty factor for scheduling during unavailable times
    private double currentScore = 0.0;
    private double bestScore = Double.NEGATIVE_INFINITY;

    public TimetableScheduler(List<Room> courseRooms, List<Room> seminarRooms, List<Teacher> teachers) {
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
        this.teacherHours = new HashMap<>();
        for (Teacher teacher : teachers) {
            teacherHours.put(teacher, 0);
        }
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

        // Check teacher availability and hours
        if (teacher != null && teacherHours.containsKey(teacher)) {
            int currentHours = teacherHours.get(teacher);
            if (currentHours + 2 > teacher.getMaxHoursPerWeek()) {
                return false; // Teacher is over their weekly limit
            }
        }

        return true;
    }

    private double calculateTimeSlotScore(Room room, String group, Teacher teacher, TimeSlot timeSlot) {
        double score = 1.0; // Base score

        // Apply penalty if teacher is scheduled during their unavailable times
        if (teacher != null && !teacher.isAvailableAt(timeSlot)) {
            score *= UNAVAILABLE_TIME_PENALTY;
        }

        return score;
    }


    private void resetScheduler() {
        schedule.clear();
        roomSchedule.clear();
        groupSchedule.clear();
        teacherSchedule.clear();
        // Reset teacher hours
        for (Teacher teacher : teacherHours.keySet()) {
            teacherHours.put(teacher, 0);
        }
    }

    public boolean generateTimetable(List<Subject> subjects) {
        resetScheduler();
        startTime = System.currentTimeMillis();
        currentScore = 0.0;
        bestScore = Double.NEGATIVE_INFINITY;

        startTime = System.currentTimeMillis();
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

        // Sort sessions by the number of available teachers (fewer teachers first)
        sessionsToSchedule.sort(Comparator.comparingInt(s -> s.getSubject().getSeminarTeachers().size()));

        boolean solutionFound = backtrack(sessionsToSchedule, 0);

        if (!solutionFound) {
            System.err.println("Error: No valid timetable could be generated. Please adjust constraints or resources.");
        }

        return solutionFound;
    }

    private boolean backtrack(List<Session> sessionsToSchedule, int index) {
        if (System.currentTimeMillis() - startTime > TIMEOUT) {
            return false; // Timeout
        }

        if (index == sessionsToSchedule.size()) {
            return currentScore > bestScore; // Found a better solution
        }

        Session session = sessionsToSchedule.get(index);
        List<Room> availableRooms = session.isCourse() ? courseRooms : seminarRooms;

        double bestLocalScore = Double.NEGATIVE_INFINITY;
        Room bestRoom = null;
        TimeSlot bestTimeSlot = null;

        for (Room room : availableRooms) {
            for (String day : days) {
                for (Integer time : timeSlots) {
                    TimeSlot timeSlot = new TimeSlot(day, time);
                    if (isTimeSlotAvailable(room, session.getGroup(), session.getTeacher(), timeSlot)) {
                        double score = calculateTimeSlotScore(room, session.getGroup(), session.getTeacher(), timeSlot);

                        if (score > bestLocalScore) {
                            bestLocalScore = score;
                            bestRoom = room;
                            bestTimeSlot = timeSlot;
                        }
                    }
                }
            }
        }

        if (bestRoom != null) {
            session.setRoom(bestRoom);
            session.setTimeSlot(bestTimeSlot);
            double previousScore = currentScore;
            currentScore += bestLocalScore;
            scheduleSession(session);

            if (backtrack(sessionsToSchedule, index + 1)) {
                bestScore = currentScore;
                return true;
            }

            // Undo the assignment
            unscheduleSession(session);
            currentScore = previousScore;
        }

        return false;
    }


    private void unscheduleSession(Session session) {
        schedule.remove(session);

        // Update room schedule
        roomSchedule.get(session.getRoom().getName()).remove(session.getTimeSlot());

        // Update group schedule
        groupSchedule.get(session.getGroup()).remove(session.getTimeSlot());

        // Update teacher schedule
        Teacher teacher = session.getTeacher();
        if (teacher != null) { // Check if the session has a teacher
            teacherHours.put(teacher, teacherHours.get(teacher) - 2);
        }
    }

    private void scheduleSession(Session session) {
        schedule.add(session);
        // Update room schedule
        roomSchedule.computeIfAbsent(session.getRoom().getName(), k -> new HashSet<>())
                   .add(session.getTimeSlot());

        // Update group schedule
        groupSchedule.computeIfAbsent(session.getGroup(), k -> new HashSet<>())
                    .add(session.getTimeSlot());

        // Update teacher schedule AND HOURS
        Teacher teacher = session.getTeacher();
        if (teacher != null) { // Check if the session has a teacher (e.g., course sessions might not)
            teacherHours.put(teacher, teacherHours.get(teacher) + 2);
        }
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
                    firstYearData.getSeminarRooms(),
                    firstYearData.getTeachers()
            );

            // Iterate through each year and generate timetables
            for (Map.Entry<Integer, TimetableData> entry : timetableDataPerYear.entrySet()) {
                Integer year = entry.getKey();
                System.out.println("\nProcessing timetable for Year " + year);
                TimetableData yearData = entry.getValue();


                // Delete any existing timetable files that are not part of the current run
                Set<Integer> yearsToProcess = timetableDataPerYear.keySet();

                File currentDir = new File(".");
                File[] files = currentDir.listFiles((dir, name) -> name.startsWith("timetable_year_") && name.endsWith(".json"));
                if (files != null) {
                    for (File file : files) {
                        String fileName = file.getName();
                        int yearToProcess = Integer.parseInt(fileName.replaceAll("\\D", ""));
                        if (!yearsToProcess.contains(yearToProcess)) {
                            file.delete();
                        }
                    }
                }

                String outputPath = "timetable_year_" + year + ".json";

                // Step 3: Generate timetable for the year

                if (scheduler.generateTimetable(yearData.getSubjects())) {
                    System.out.println("Successfully generated timetable for Year " + year);

                    System.out.println(scheduler.getSchedule());

                    scheduler.exportToJson(outputPath);
                    System.out.println("Exported timetable to " + outputPath);
                } else {
                    System.out.println("Failed to generate a valid timetable for Year " + year);
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
