# How to run

## Prerequisites

### 1. Add a input.json file to the project root directory with the following content:

```json
{
  "rooms": {
    "courseRooms": ["C2", "C309", "C112", "C308"],
    "seminarRooms": [
      "C210", "C308", "C309", "C410", "C411", "C412",
      "C413", "C403", "C901", "C903", "C905", "C910"
    ]
  },
  "teachers": [
    
    {
      "name": "Prof. Taylor",
      "role": "LECTURER"
    },
    {
      "name": "Ms. Miller",
      "role": "ASSISTANT"
    }
    ...
  ],
  "subjects": [
    {
      "name": "Computer Networks",
      "courseTeacher": "Prof. Smith",
      "seminarTeachers": ["Mr. Brown", "Ms. Miller", "Mr. Wilson"]
    },
    {
      "name": "Database Systems",
      "courseTeacher": "Prof. Johnson",
      "seminarTeachers": ["Ms. Thompson", "Mr. Garcia", "Ms. Martinez"]
    }
    ...
  ]
}
```

### 2. Run the TimetableSchedulerApplication.java file from org/example/solver/TimetableScheduler.java

### 3. Turn on the Spring server from org/example/springApp/TimetableApplication.java and go to http://localhost:8080/timetable
