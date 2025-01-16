import json
import time
import pandas as pd
from reportlab.lib import colors
from reportlab.lib.pagesizes import letter
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle
from pandas.api.types import CategoricalDtype

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

# Backtracking function -> assignment = {index: {'timeslot' : timeslot, 'course' : course, 'prof' : prof, 'group' : group}}
def is_consistent(assignment, ts, c, profesor, g):
    # Profesorul poate tine cursuri doar la materiile pe care le predă
    if c not in profs_subjects.get(profesor, []):
        return False
    
    # O grupa participa doar la cursurile aferente anului ei
    year = g[0]
    if c not in courses_per_year.get(year, []):
        return False

    # Un profesor nu poate preda doua cursuri in acelasi interval orar
    for a in assignment:
        prof = assignment[a]['prof']
        timeslot = assignment[a]['timeslot']
        if prof == profesor and timeslot % 30 == ts % 30:
            return False

    # O sală nu poate găzdui două cursuri în același interval de timp.
    for a in assignment:
        timeslot = assignment[a]['timeslot']
        if ts == timeslot:
            return False  # Room time conflict

    # O grupa un poate participa la doua cursuri simultan
    for a in assignment:
        group = assignment[a]['group']
        timeslot = assignment[a]['timeslot']
        if group == g and timeslot % 30 == ts % 30:
            return False

    return True

def backtrack(assignment, index):
    if index == len(classes_to_course):
        return True # Toate cursurile au fost programate

    class_info = classes_to_course[index]
    group_to_add = class_info['group']
    course_to_add = class_info['course']

    # Profii ce pot preda cursul
    possible_profs = []
    for prof in profs_subjects:
        if course_to_add in profs_subjects[prof]:
            possible_profs.append(prof)

    for timeslot_to_add in timeslots:
        for prof_to_add in possible_profs:
            if is_consistent(assignment, timeslot_to_add, course_to_add, prof_to_add, group_to_add):
                assignment[index] = {
                    'timeslot': timeslot_to_add,
                    'course': course_to_add,
                    'prof': prof_to_add,
                    'group': group_to_add
                }
                if backtrack(assignment, index + 1):
                    return True
                del assignment[index]
    return False

start_time = time.time()
assignment = {}
if backtrack(assignment, 0):
    end_time = time.time()
    print(f"Orarul a fost generat cu succes prin algoritmul BKT în {end_time - start_time} secunde.")
    import pandas as pd
from pandas.api.types import CategoricalDtype

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

pdf_file = 'orar.pdf'
pdf = SimpleDocTemplate(pdf_file, pagesize=letter)
data_table = [df.columns.tolist()] + df.values.tolist()
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

elems = [table]
pdf.build(elems)
print(f"Orarul a fost salvat în fișierul {pdf_file}.")