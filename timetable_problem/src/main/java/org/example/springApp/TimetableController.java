package org.example.springApp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;
import org.example.data.TimetableData;
import org.example.data.config.TimetableConfigLoader;
import org.example.solver.TimetableScheduler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;
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

        public void adjustForYear(int year) {
            // Prefix the group and series with the year
            if (group != null) {
                this.group = year + this.group; // E.g., "A1" becomes "1A1" for year 1
            }
        }

    }



    private static class TimetableExport {
        public List<ScheduleEntry> schedule;
    
        public List<ScheduleEntry> getSchedule() {
            return schedule;
        }
    }
    @GetMapping("/timetable")
    public String getYearlyTimetableLinks(Model model) {
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
                TimetableData yearData = entry.getValue();
                String outputPath = "timetable_year_" + year + ".json";

                // Step 3: Generate timetable for the year
                if (scheduler.generateTimetable(yearData.getSubjects())) {
                    scheduler.exportToJson(outputPath);
                } else {
                    model.addAttribute("errorMessage", "Failed to generate a valid timetable for Year " + year);
                    return "timetable_links"; // Return the view with the error message
                }
            }

            model.addAttribute("years", timetableDataPerYear.keySet());
            return "timetable_links"; // A view that displays links for each year

        } catch (IOException | IllegalStateException e) {
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            return "timetable_links"; // Return the view with the error message
        }
    }

    @GetMapping("/timetable/year")
    public String getFilteredTimetableForYear(
            @RequestParam("year") int year,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "name", required = false) String name,
            Model model) throws IOException {

        // Load the timetable JSON for the given year
        ObjectMapper mapper = new ObjectMapper();
        TimetableExport timetable = mapper.readValue(new File("timetable_year_" + year + ".json"), TimetableExport.class);

        // Adjust series and group names to include the year prefix
        List<ScheduleEntry> adjustedEntries = timetable.getSchedule();
        adjustedEntries.forEach(entry -> entry.adjustForYear(year));

        // Filter timetable entries if a filter is provided (type + name)
        List<ScheduleEntry> filteredEntries = adjustedEntries;
        if (type != null && name != null) {
            switch (type.toLowerCase()) {
                case "subject":
                    filteredEntries = filteredEntries.stream()
                            .filter(entry -> name.equals(entry.subject))
                            .collect(Collectors.toList());
                    break;
                case "group":
                    String yearPrefix = String.valueOf(year);
                    if (name.length() == 2 && name.startsWith(yearPrefix)) {
                        // Series filter (e.g., "1A")
                        String series = name.substring(1); // Get "A" from "1A"
                        filteredEntries = filteredEntries.stream()
                                .filter(entry ->
                                        // Include exact series match (for courses)
                                        name.equals(entry.group) ||
                                                // Include all groups of this series (e.g., 1A1, 1A2, etc.)
                                                (entry.group != null && entry.group.startsWith(name)))
                                .collect(Collectors.toList());
                    } else {
                        // Specific group filter (e.g., "1A1", "2B3", etc.)
                        String series = name.substring(0, 2); // Get "1A" from "1A1" or "2B" from "2B3"
                        filteredEntries = filteredEntries.stream()
                                .filter(entry ->
                                        // Include the specific group's sessions
                                        name.equals(entry.group) ||
                                                // Include the corresponding series courses
                                                series.equals(entry.group))
                                .collect(Collectors.toList());
                    }
                    break;
                case "teacher":
                    filteredEntries = filteredEntries.stream()
                            .filter(entry -> name.equals(entry.teacher))
                            .collect(Collectors.toList());
                    break;
                default:
                    throw new IllegalArgumentException("Invalid filter type: " + type);
            }
        }

        // Add attributes for the view
        model.addAttribute("year", year);
        model.addAttribute("type", type);
        model.addAttribute("name", name);
        model.addAttribute("timetable", filteredEntries);
        model.addAttribute("daysOfWeek", Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"));

        return "timetable_view";
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
            TimetableScheduler scheduler = new TimetableScheduler(data.getCourseRooms(), data.getSeminarRooms(),data.getTeachers());

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

    @GetMapping("/nlptest")
    public String showNlpTestPage() {
        return "nlptest";
    }

    private static final String OPENAI_API_KEY = "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";
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private String callOpenAiForTimetable(String userInput) throws IOException {
        // Store schema as a regular string to avoid escaping issues
        String schema = """
        {
            "type": "object",
            "properties": {
                "teachers": {
                    "type": "array",
                    "items": {
                        "type": "object",
                        "properties": {
                            "name": {"type": "string"},
                            "role": {"type": "string: LECTURER or ASSISTANT"},
                            "maxHoursPerWeek": {"type": "number"},
                            "unavailableTimes": {
                                "type": "array",
                                "items": {"type": "string", example: ["Monday 9-11", "Wednesday 14-16"]}
                            }
                        }
                    }
                },
                "rooms": {
                    "type": "object",
                    "properties": {
                        "courseRooms": {"type": "array", "items": {"type": "string"}},
                        "seminarRooms": {"type": "array", "items": {"type": "string"}}
                    }
                },
                "years": {
                    "type": "number",
                    "additionalProperties": {
                        "type": "object",
                        "properties": {
                            "subjects": {
                                "type": "array",
                                "items": {
                                    "type": "object",
                                    "properties": {
                                        "name": {"type": "string"},
                                        "courseTeacher": {"type": "string"},
                                        "seminarTeacher": {"type": "array", "items": {"type": "string"}}
                                    }
                                }
                            },
                            "series": {"type": "array", "items": {"type": "string: A, B, C, etc."}},
                            "groups": {
                                "type": "object",
                                "additionalProperties": {"type": "number"}
                            }
                        }
                    }
                }
            }
        }
        """;

        // Create the messages array as a JsonNode to ensure proper JSON structure
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode requestJson = mapper.createObjectNode();
        requestJson.put("model", "gpt-4o");
        requestJson.put("temperature", 0.7);
        requestJson.put("max_tokens", 2000);
        
        ObjectNode responseFormat = mapper.createObjectNode();
        responseFormat.put("type", "json_object");
        requestJson.set("response_format", responseFormat);

        ArrayNode messages = mapper.createArrayNode();
        
        ObjectNode systemMessage = mapper.createObjectNode();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a timetable generator. Generate valid JSON matching the schema: " + schema + "Also, the series A,B,C should be used for groups. Example groups: {A: 2, B: 1}, meaning that series A has 2 groups. Use a reasonable ammounts of groups if they are not specified.");
        
        ObjectNode userMessage = mapper.createObjectNode();
        userMessage.put("role", "user");
        userMessage.put("content", userInput);
        
        messages.add(systemMessage);
        messages.add(userMessage);
        requestJson.set("messages", messages);

        // Convert the JSON object to a string
        String requestBody = mapper.writeValueAsString(requestJson);

        Request request = new Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
            .post(RequestBody.create(MediaType.parse("application/json"), requestBody))
            .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (!response.isSuccessful()) {
                ObjectMapper errorMapper = new ObjectMapper();
                JsonNode errorNode = errorMapper.readTree(responseBody);
                String errorMessage = errorNode.path("error").path("message").asText("Unknown error");
                String detailedError = String.format("""
                    API Error (HTTP %d):
                    Message: %s
                    
                    Request Details:
                    URL: %s
                    Model: %s
                    Temperature: 0.7
                    Max Tokens: 2000
                    """, 
                    response.code(), 
                    errorMessage,
                    request.url(),
                    "gpt-4"
                );
                throw new IOException(detailedError);
            }
            return responseBody;
        }
    }

    @PostMapping("/nlptest")
    public String generateTimetable(@RequestParam("userInput") String userInput, Model model) {
        try {
            String openAiResponse = callOpenAiForTimetable(userInput);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(openAiResponse);
            String content = root.path("choices").path(0).path("message").path("content").asText();

            // Format the JSON for display
            String prettyJson = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(mapper.readValue(content, Object.class));

            // Add both raw and formatted JSON to the model
            model.addAttribute("generatedTimetable", prettyJson);
            model.addAttribute("rawJsonData", content); // This will be used by JavaScript to populate the form

        } catch (Exception e) {
            model.addAttribute("generatedTimetable", "Error: " + e.getMessage());
        }
        return "nlptest";
    }

    @PostMapping("/saveJson")
    public ResponseEntity<String> saveJson(@org.springframework.web.bind.annotation.RequestBody String jsonContent) {
        try {
            Path path = Paths.get("input.json");
            Files.write(path, jsonContent.getBytes());
            return ResponseEntity.ok("JSON file saved successfully.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save JSON file.");
        }
    }

}