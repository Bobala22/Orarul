import json
import time
import pandas as pd
from reportlab.lib import colors
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer
from reportlab.lib.styles import getSampleStyleSheet
from pandas.api.types import CategoricalDtype
from collections import deque
import copy

with open('input.json', 'r') as f:
    data = json.load(f)

groups = data['groups']
courses_per_year = data['courses']
profs_subjects = data['profs_subjects']
rooms = data['rooms']

days = ['Monday']
time_intervals = [
    '8:00-10:00',
    '10:00-12:00',
    '12:00-14:00',
    '14:00-16:00',
    '16:00-18:00',
    '18:00-20:00'
]

timeslot_name = {}
index = 0
for room in rooms:
    for day in days:
        for interval in time_intervals:
            timeslot_name[index] = f"{day} {interval} {room}"
            index += 1

timeslots = list(range(len(timeslot_name)))

classes_to_course = []
for group in groups:
    year = group[0]  
    for course in courses_per_year[year]:
        classes_to_course.append({'group': group, 'course': course})

variables = list(range(len(classes_to_course)))  

domains = {}
for index, class_info in enumerate(classes_to_course):
    group = class_info['group']
    course = class_info['course']
    year = group[0]

    possible_profs = [prof for prof in profs_subjects if course in profs_subjects[prof]]

    domain = []
    for prof in possible_profs:
        for timeslot in timeslots:
            domain.append({'timeslot': timeslot, 'prof': prof})
    domains[index] = domain

# Store a deep copy of the initial domains
initial_domains = copy.deepcopy(domains)

def is_consistent(var1, val1, var2, val2):
    class1 = classes_to_course[var1]
    group1 = class1['group']
    course1 = class1['course']
    prof1 = val1['prof']
    timeslot1 = val1['timeslot']

    class2 = classes_to_course[var2]
    group2 = class2['group']
    course2 = class2['course']
    prof2 = val2['prof']
    timeslot2 = val2['timeslot']

    # Un profesor nu poate preda doua cursuri in acelasi interval orar
    if prof1 == prof2 and timeslot1 % 30 == timeslot2 % 30:
        return False

    # O grupa un poate participa la doua cursuri simultan
    if group1 == group2 and timeslot1 % 30 == timeslot2 % 30:
        return False

    # O sală nu poate găzdui două cursuri în același interval de timp.
    if timeslot1 == timeslot2:
        return False

    return True

def AC3(variables, domains):
    queue = deque()
    # all arcs in queue
    for var in variables:
        for neighbor in variables:
            if var != neighbor:
                queue.append((var, neighbor))

    while queue:
        (xi, xj) = queue.popleft()
        if revise(xi, xj, domains):
            if not domains[xi]:
                return False  # Domain is empty
            for xk in variables:
                if xk != xi and xk != xj:
                    queue.append((xk, xi))
    return True

def revise(xi, xj, domains):
    revised = False
    for x in domains[xi][:]:
        if not any(is_consistent(xi, x, xj, y) for y in domains[xj]):
            domains[xi].remove(x)
            revised = True
    return revised


with open('output_init.txt', 'w') as file:
    for var in variables:
        class_info = classes_to_course[var]
        file.write(f"Variable {var} (Group: {class_info['group']}, Course: {class_info['course']}):\n")
        for val in domains[var]:
            prof = val['prof']
            timeslot = val['timeslot']
            file.write(f"  Prof: {prof}, Timeslot: {timeslot_name[timeslot]}\n")

if AC3(variables, domains):
    print("AC3: The problem is arc-consistent.")
else:
    print("AC3: The problem is not arc-consistent.")

with open('output_update.txt', 'w') as file:
    for var in variables:
        class_info = classes_to_course[var]
        file.write(f"Variable {var} (Group: {class_info['group']}, Course: {class_info['course']}):\n")
        for val in domains[var]:
            prof = val['prof']
            timeslot = val['timeslot']
            file.write(f"  Prof: {prof}, Timeslot: {timeslot_name[timeslot]}\n")