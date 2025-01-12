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

    public TimetableData loadConfiguration(String configFilePath) throws IOException {
        // Read the configuration file
        TimetableConfig config = mapper.readValue(new File(configFilePath), TimetableConfig.class);

        // Create rooms
        List<Room> courseRooms = config.getRooms().getCourseRooms().stream()
                .map(name -> new Room(name, true))
                .collect(Collectors.toList());

        List<Room> seminarRooms = config.getRooms().getSeminarRooms().stream()
                .map(name -> new Room(name, false))
                .collect(Collectors.toList());

        // Create teachers map
        Map<String, Teacher> teacherMap = new HashMap<>();
        for (TeacherConfig teacherConfig : config.getTeachers()) {
            Teacher teacher = new Teacher(teacherConfig.getName(), teacherConfig.getRole());
            teacherMap.put(teacher.getName(), teacher);
        }

        // Create subjects
        List<Subject> subjects = new ArrayList<>();
        for (SubjectConfig subjectConfig : config.getSubjects()) {
            // Get course teacher
            Teacher courseTeacher = teacherMap.get(subjectConfig.getCourseTeacherName());
            if (courseTeacher == null) {
                throw new IllegalStateException(
                        "Course teacher not found: " + subjectConfig.getCourseTeacherName());
            }

            // Get seminar teachers
            List<Teacher> seminarTeachers = new ArrayList<>();
            for (String teacherName : subjectConfig.getSeminarTeacherNames()) {
                Teacher teacher = teacherMap.get(teacherName);
                if (teacher == null) {
                    throw new IllegalStateException("Seminar teacher not found: " + teacherName);
                }
                seminarTeachers.add(teacher);
            }

            subjects.add(new Subject(subjectConfig.getName(), courseTeacher, seminarTeachers));
        }

        return new TimetableData(subjects, courseRooms, seminarRooms);
    }
}
