import json
import sys
import pandas as pd
from reportlab.lib import colors
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph, Spacer
from reportlab.lib.styles import getSampleStyleSheet
from pandas.api.types import CategoricalDtype
from collections import deque
import copy
import time

with open('input.json', 'r') as f:
    data = json.load(f)

groups = data['groups']
courses_per_year = data['courses']
profs_subjects = data['profs_subjects']
rooms = data['rooms']

days = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday']
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

# Generate list of all classes to schedule (group, course)
classes_to_course = []
for group in groups:
    year = group[0]  # group names start with year number
    for course in courses_per_year[year]:
        classes_to_course.append({'group': group, 'course': course})

variables = list(range(len(classes_to_course)))  # Variables are indices of classes_to_course

def initialize_domains():
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
    return domains

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

# AC-3 Algorithm
def AC3(variables, domains, assignment):
    queue = deque()
    for xi in variables:
        for xj in variables:
            if xi != xj:
                queue.append((xi, xj))

    while queue:
        (xi, xj) = queue.popleft()
        if revise(xi, xj, domains, assignment):
            if not domains[xi]:
                return False  # Domain is empty
            for xk in variables:
                if xk != xi and xk != xj:
                    queue.append((xk, xi))
    return True

def revise(xi, xj, domains, assignment):
    revised = False
    for x in domains[xi][:]:
        if xi in assignment and assignment[xi] != x:
            continue
        if not any(is_consistent(xi, x, xj, y) for y in domains[xj]):
            domains[xi].remove(x)
            revised = True
    return revised

# BKT with AC-3
def backtrack(assignment, domains):
    if len(assignment) == len(variables):
        return assignment

    # alegem variabila cu cele mai putine valori in domeniu
    unassigned_vars = [v for v in variables if v not in assignment]
    var = min(unassigned_vars, key=lambda v: len(domains[v]))

    for value in domains[var]:
        local_assignment = assignment.copy()
        local_assignment[var] = value

        local_domains = copy.deepcopy(domains)
        local_domains[var] = [value]

        if AC3(variables, local_domains, local_assignment):
            result = backtrack(local_assignment, local_domains)
            if result:
                return result

    return False

# Initialize domains
domains = initialize_domains()

# Timing Backtracking with AC3
start_time_bt_ac3 = time.time()
ac3_assignment = backtrack({}, domains)
end_time_bt_ac3 = time.time()

# Output results
if ac3_assignment:
    print(f"Backtracking with AC3 found a solution in {end_time_bt_ac3 - start_time_bt_ac3:.4f} seconds.")
else:
    print("Backtracking with AC3 did not find a solution.")
    sys.exit()

# Assign final_assignment using ac3_assignment
final_assignment = ac3_assignment

# Generate schedule
data_rows = []
for index, value in final_assignment.items():
    class_info = classes_to_course[index]
    group = class_info['group']
    course = class_info['course']
    prof = value['prof']
    timeslot = value['timeslot']
    day, time_interval, room = timeslot_name[timeslot].split()

    data_rows.append({
        'Grupa': group,
        'Curs': course,
        'Profesor': prof,
        'Zi': day,
        'Interval': time_interval,
        'Sala': room
    })

# Define the correct order for days
days_order = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday']
day_type = CategoricalDtype(categories=days_order, ordered=True)
df = pd.DataFrame(data_rows)
df['Zi'] = df['Zi'].astype(day_type)

# Define the correct order for intervals
interval_order = [
    '8:00-10:00',
    '10:00-12:00',
    '12:00-14:00',
    '14:00-16:00',
    '16:00-18:00',
    '18:00-20:00'
]
interval_type = CategoricalDtype(categories=interval_order, ordered=True)
df['Interval'] = df['Interval'].astype(interval_type)

# Sort the DataFrame
df = df.sort_values(['Grupa', 'Zi', 'Interval'])

# Generate the PDF schedule
pdf_file = 'orar_integrated.pdf'
pdf = SimpleDocTemplate(pdf_file, pagesize=letter)
elems = []
styles = getSampleStyleSheet()

# Group by 'Grupa' and create separate tables
for group, group_df in df.groupby('Grupa'):
    elems.append(Paragraph(f"Grupa: {group}", styles['Heading2']))
    data_table = [group_df.columns.tolist()] + group_df.values.tolist()
    table = Table(data_table)

    style = TableStyle([
        ('BACKGROUND', (0,0), (-1,0), colors.gray),
        ('TEXTCOLOR', (0,0), (-1,0), colors.whitesmoke),
        ('ALIGN', (0,0), (-1,-1), 'CENTER'),
        ('FONTNAME', (0,0), (-1,0), 'Helvetica-Bold'),
        ('BOTTOMPADDING', (0,0), (-1,0), 12),
        ('GRID', (0,0), (-1,-1), 1, colors.black),
        ('FONTSIZE', (0,0), (-1,-1), 8),
    ])
    table.setStyle(style)
    elems.append(table)
    elems.append(Spacer(1, 12))

pdf.build(elems)
print(f"Orarul a fost salvat în fișierul {pdf_file}.")