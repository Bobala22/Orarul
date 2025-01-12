package org.example.springApp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TimetableController {

    private static class ScheduleEntry {
        public String day;
        public int startTime;
        public int endTime;
        public String subject;
        public String type;
        public String group;
        public String teacher;
        public String teacherRole;
        public String room;
    }

    private static class TimetableExport {
        public List<ScheduleEntry> schedule;
    }

    @GetMapping("/timetable")
    public String getTimetable(@RequestParam(required = false) String type, @RequestParam(required = false) String name, Model model) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TimetableExport timetable = mapper.readValue(new File("timetable_output.json"), TimetableExport.class);

        List<ScheduleEntry> filteredSchedule;
        if (type == null || name == null) {
            filteredSchedule = timetable.schedule;
        } else {
            switch (type) {
                case "group":
                    filteredSchedule = timetable.schedule.stream()
                            .filter(entry -> entry.group.equals(name))
                            .collect(Collectors.toList());
                    break;
                case "teacher":
                    filteredSchedule = timetable.schedule.stream()
                            .filter(entry -> entry.teacher.equals(name))
                            .collect(Collectors.toList());
                    break;
                case "subject":
                    filteredSchedule = timetable.schedule.stream()
                            .filter(entry -> entry.subject.equals(name))
                            .collect(Collectors.toList());
                    break;
                default:
                    throw new IllegalArgumentException("Invalid type: " + type);
            }
        }

        model.addAttribute("timetable", filteredSchedule);
        model.addAttribute("type", type);
        model.addAttribute("name", name);
        model.addAttribute("daysOfWeek", Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"));
        return "timetable";
    }
}