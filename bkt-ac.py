import json
import time
import pandas as pd
from reportlab.lib import colors
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle
from pandas.api.types import CategoricalDtype
from collections import deque

# Load data from input.json
with open('input.json', 'r') as f:
    data = json.load(f)

groups = data['groups']
courses_per_year = data['courses']
profs_subjects = data['profs_subjects']
rooms = data['rooms']


# Define days of the week and time intervals
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

timeslots = list(range(0, 600))

# Generate list of all classes to schedule (group, course)
classes_to_course = []
for group in groups:
    year = group[0]  # Assuming group names start with year number
    for course in courses_per_year[year]:
        classes_to_course.append({'group': group, 'course': course})

# Initialize domains for each variable (class to schedule)
domains = {}
for index, class_info in enumerate(classes_to_course):
    course = class_info['course']
    group = class_info['group']
    year = group[0]
    possible_profs = [prof for prof in profs_subjects if course in profs_subjects[prof]]
    domain = []
    for prof in possible_profs:
        for ts in timeslots:
            # Only consider timeslots that match the group's year courses
            if course in courses_per_year.get(year, []):
                domain.append({'timeslot': ts, 'prof': prof})
    domains[index] = domain

# AC-3 Algorithm
def AC3(domains, assignment):
    queue = deque()
    variables = list(domains.keys())
    for var in variables:
        for neighbor in variables:
            if var != neighbor:
                queue.append((var, neighbor))
    while queue:
        (xi, xj) = queue.popleft()
        if revise(xi, xj, domains, assignment):
            if not domains[xi]:
                return False
            for xk in variables:
                if xk != xi and xk != xj:
                    queue.append((xk, xi))
    return True

def revise(xi, xj, domains, assignment):
    revised = False
    for x in domains[xi][:]:
        if not any(is_arc_consistent(xi, x, xj, y, assignment) for y in domains[xj]):
            domains[xi].remove(x)
            revised = True
    return revised

def is_arc_consistent(var1, val1, var2, val2, assignment):
    # Combine assigned values with current values, including 'group' and 'course'
    temp_assignment = assignment.copy()
    temp_assignment[var1] = {
        'timeslot': val1['timeslot'],
        'prof': val1['prof'],
        'course': classes_to_course[var1]['course'],
        'group': classes_to_course[var1]['group']
    }
    temp_assignment[var2] = {
        'timeslot': val2['timeslot'],
        'prof': val2['prof'],
        'course': classes_to_course[var2]['course'],
        'group': classes_to_course[var2]['group']
    }
    # Check consistency
    return is_consistent(temp_assignment, var1) and is_consistent(temp_assignment, var2)

def is_consistent(assignment, index):
    # Check if current assignment is consistent
    current = assignment[index]
    timeslot = current['timeslot']
    prof = current['prof']
    group = classes_to_course[index]['group']
    # Unchanged code from previous is_consistent function
    for a in assignment:
        if a != index:
            other = assignment[a]
            if other['prof'] == prof and other['timeslot'] % 30 == timeslot % 30:
                return False
            if other['group'] == group and other['timeslot'] % 30 == timeslot % 30:
                return False
            if other['timeslot'] == timeslot:
                return False
    return True

def backtrack(assignment, domains):
    if len(assignment) == len(classes_to_course):
        return assignment
    # Select unassigned variable
    var = min((v for v in domains if v not in assignment), key=lambda v: len(domains[v]))
    # Copy domains to restore later if needed
    local_domains = {v: list(domains[v]) for v in domains}
    for value in domains[var]:
        # Assign value
        assignment[var] = {
            'timeslot': value['timeslot'],
            'prof': value['prof'],
            'course': classes_to_course[var]['course'],
            'group': classes_to_course[var]['group']
        }
        # Enforce arc consistency
        domains[var] = [value]
        if AC3(domains, assignment):
            result = backtrack(assignment, domains)
            if result:
                return result
        # Backtrack
        del assignment[var]
        domains = {v: list(local_domains[v]) for v in local_domains}
    return False

start_time = time.time()
assignment = {}
result = backtrack(assignment, domains)
if result:
    end_time = time.time()
    print(f"Orarul a fost generat cu succes prin algoritmul BKT+AC3 în {end_time - start_time} secunde.")
    data_rows = []
    for line in assignment.values():
        group = line['group']
        course = line['course']
        prof = line['prof']
        day = timeslot_name[line['timeslot']].split()[0]
        time_interval = timeslot_name[line['timeslot']].split()[1]
        room = timeslot_name[line['timeslot']].split()[2]
        data_rows.append({
            'Grupa': group,
            'Curs': course,
            'Profesor': prof,
            'Zi': day,
            'Interval': time_interval,
            'Sala': room
        })

    days_order = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday']
    day_type = CategoricalDtype(categories=days_order, ordered=True)
    df = pd.DataFrame(data_rows)
    df['Zi'] = df['Zi'].astype(day_type)

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

    # Sort the DataFrame by 'Zi', 'Interval', and 'Grupa'
    df = df.sort_values(['Zi', 'Interval', 'Grupa'])

    pdf_file = 'orar-bkt-ac.pdf'
    pdf = SimpleDocTemplate(pdf_file, pagesize=letter)
    elems = []

    # Group the DataFrame by 'Grupa'
    grouped = df.groupby('Grupa')

    from reportlab.platypus import Paragraph, Spacer
    from reportlab.lib.styles import getSampleStyleSheet

    styles = getSampleStyleSheet()

    for group_name, group_df in grouped:
        # Add a heading for the group
        group_title = Paragraph(f"Orar pentru grupa {group_name}", styles['Heading2'])
        elems.append(group_title)
        elems.append(Spacer(1, 12))
        # Create the table for the group
        data_table = [group_df.columns.tolist()] + group_df.values.tolist()
        table = Table(data_table)
        style = TableStyle([
            # ...existing style code...
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
        elems.append(Spacer(1, 24))  # Add space after each table

    pdf.build(elems)
    print(f"Orarul a fost salvat în fișierul {pdf_file}.")