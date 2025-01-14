package org.example.data.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.data.entities.Room;
import org.example.data.entities.Subject;
import org.example.data.entities.Teacher;
import org.example.data.TimetableData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Configuration loader class
public class TimetableConfigLoader {
    private final ObjectMapper mapper;

    public TimetableConfigLoader() {
        this.mapper = new ObjectMapper();
    }

    public Map<Integer, TimetableData> loadConfiguration(String configFilePath) throws IOException {
        TimetableConfig config = mapper.readValue(new File(configFilePath), TimetableConfig.class);

        // Parse the shared courseRooms and seminarRooms
        List<Room> courseRooms = config.getRooms().getCourseRooms().stream()
                .map(name -> new Room(name, true))
                .collect(Collectors.toList());

        List<Room> seminarRooms = config.getRooms().getSeminarRooms().stream()
                .map(name -> new Room(name, false))
                .collect(Collectors.toList());

        // Parse timetable data for each year
        Map<Integer, TimetableData> timetableDataPerYear = new HashMap<>();

        for (Map.Entry<Integer, YearConfig> entry : config.getYears().entrySet()) {
            Integer year = entry.getKey();
            YearConfig yearConfig = entry.getValue();

            Map<String, Teacher> teacherMap = new HashMap<>();
            for (TeacherConfig teacherConfig : yearConfig.getTeachers()) {
                teacherMap.put(
                        teacherConfig.getName(),
                        new Teacher(teacherConfig.getName(), teacherConfig.getRole())
                );
            }

            List<Subject> subjects = new ArrayList<>();
            for (SubjectConfig subjectConfig : yearConfig.getSubjects()) {
                Teacher courseTeacher = teacherMap.get(subjectConfig.getCourseTeacherName());
                if (courseTeacher == null || !courseTeacher.canTeachCourse()) {
                    throw new IllegalArgumentException("Invalid course teacher: " + subjectConfig.getCourseTeacherName());
                }

                List<Teacher> seminarTeachers = subjectConfig.getSeminarTeacherNames().stream()
                        .map(teacherName -> {
                            Teacher seminarTeacher = teacherMap.get(teacherName);
                            if (seminarTeacher == null) {
                                throw new IllegalArgumentException("Invalid seminar teacher: " + teacherName);
                            }
                            return seminarTeacher;
                        }).collect(Collectors.toList());

                subjects.add(new Subject(subjectConfig.getName(), courseTeacher, seminarTeachers));
            }

            timetableDataPerYear.put(
                    year,
                    new TimetableData(subjects, courseRooms, seminarRooms)
            );
        }

        return timetableDataPerYear;
    }
}
