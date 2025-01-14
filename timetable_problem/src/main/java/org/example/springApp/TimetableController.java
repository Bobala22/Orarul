package org.example.springApp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.data.TimetableData;
import org.example.solver.TimetableScheduler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.example.solver.TimetableScheduler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @GetMapping("/newpage")
    public String getNewPage(Model model) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TimetableExport timetable = mapper.readValue(new File("timetable_output.json"), TimetableExport.class);

        model.addAttribute("timetable", timetable.schedule);
        model.addAttribute("daysOfWeek", Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"));
        return "newpage";
    }

    @GetMapping("/timetableinput")
    public String getTimetableInput(Model model) {
        // Add any necessary attributes to the model
        return "timetableinput"; // Ensure you have a corresponding view named 'timetableinput.html'
    }

    // DOES NOT WORK PROPERLY
    @PostMapping("/uploadTimetable")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a file to upload.");
            return "timetableinput";
        }

        try {
            // Save the file to a location or process it as needed
            byte[] bytes = file.getBytes();
            Path path = Paths.get("uploaded_files/" + file.getOriginalFilename());
            Files.write(path, bytes);

            // Process the file (e.g., parse JSON and update the timetable)
            ObjectMapper mapper = new ObjectMapper();
            TimetableData data = mapper.readValue(path.toFile(), TimetableData.class);

            // Initialize the TimetableScheduler with the necessary data
            TimetableScheduler scheduler = new TimetableScheduler(data.getCourseRooms(), data.getSeminarRooms());

            // Generate the timetable
            if (scheduler.generateTimetable(data.getSubjects())) {
                model.addAttribute("timetable", scheduler.getSchedule());
                model.addAttribute("message", "File uploaded and processed successfully: " + file.getOriginalFilename());
                return "redirect:/timetable";
            } else {
                model.addAttribute("message", "Failed to generate a valid timetable.");
            }
        } catch (IOException e) {
            model.addAttribute("message", "Failed to upload file: " + e.getMessage());
        } catch (IllegalStateException e) {
            model.addAttribute("message", "Configuration error: " + e.getMessage());
        }

        return "timetableinput";
    }
}