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

        // Create a map of global teachers
        Map<String, Teacher> globalTeacherMap = config.getTeachers().stream()
                .collect(Collectors.toMap(TeacherConfig::getName, tc -> new Teacher(tc.getName(), tc.getRole())));

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

            List<Subject> subjects = new ArrayList<>();
            for (SubjectConfig subjectConfig : yearConfig.getSubjects()) {
                Teacher courseTeacher = globalTeacherMap.get(subjectConfig.getCourseTeacher()); // Get from the global map
                if (courseTeacher == null || !courseTeacher.canTeachCourse()) {
                    throw new IllegalArgumentException("Invalid course teacher: " + subjectConfig.getCourseTeacher());
                }


                List<Teacher> seminarTeachers = subjectConfig.getSeminarTeacher().stream()
                        .map(globalTeacherMap::get) // Directly get teachers from the global map
                        .collect(Collectors.toList());

                subjects.add(new Subject(subjectConfig.getName(), courseTeacher, seminarTeachers));
            }

            timetableDataPerYear.put(year, new TimetableData(subjects, courseRooms, seminarRooms));
        }

        return timetableDataPerYear;
    }
}
