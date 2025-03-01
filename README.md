# Timetable
## Project for the artificial inteligence course final assignment - UAIC - FII 
<br>Authors:
Albert Alexandru, 
Isache Bogdan, 
Moloce Alin 
<br><br><br>
<h1>Tasks</h1>
<br><b>Moloce Alin</b> 

- Java Backtracking Implementation for better solution generation performance (50% faster than old Python version)
- Java Spring timetable pages at localhost:8080/timetable .Filtering available for custom timetables (Year Timetable,Series Timetable, Group Timetable, Teacher Timetable, Subject Timetable)
<br>![localhost:8080/timetable preview](https://github.com/Bobala22/Orarul/blob/main/links.png?raw=true)
![localhost:8080/timetable preview](https://github.com/Bobala22/Orarul/blob/main/year_1.png?raw=true)

 

<br><b>Isache Bogdan</b>

- Modeling the problem into variables with their domains that facilitate the representation of the problem alongside with hard and soft constraints in order for the timetable to be correct and fair and also try to respect the prefferences of teachers.
- Creating the input filed section on the main page, json building functionality, and json saving/importing/modifying functionality
<br> ![main-page preview](main-page.png)

<br><b>Albert Alexandru</b>

- Implemented natural language processing (NLP) to translate user input into JSON input for the timetable generation algorithm.
- Worked on a web page that converts user requirements expressed in natural language into input for the timetable solver.

<h2>Improvements to the proposed project</h2>

- friendly web user interface made with Spring Java API that allows the customer to make his own timetable in a very intuitively manner
- json import of an old version of the timetable and the posibility of modifing it
- alternative of generating a timetable with natural language through the usage of prompt enginnering and GPT4.0
- adding soft constraints for the teachers: maximum hours per week and their unavailable hours in a day
