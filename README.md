# Course Scheduler

A constraint-satisfaction backtracking scheduler that assigns university courses to time slots and classrooms, respecting instructor availability, room capacity, and program conflict rules.

---

## Project Structure

```
.
├── Main.java
├── Scheduler.java
├── Course.java
├── Classroom.java
├── InstructorAvailability.java
├── CSVLoader.java
├── courses.csv
├── rooms.csv
└── availability.csv
```

---

## Prerequisites

| Requirement | Version |
|---|---|
| Java JDK | 8 or higher |

No external libraries or build tools required — pure Java.

To check your Java version:
```bash
java -version
javac -version
```

---

## CSV File Format

All three CSV files must be in the **same directory** as your compiled classes (or wherever you run the program from).

### `courses.csv`
```
id,program,year,type,instructor,semester,enrollment
C0001,CS,1,Core,I001,2,45
C0002,CS,1,Lab,I002,1,20
```

| Column | Description |
|---|---|
| `id` | Unique course ID |
| `program` | Program code (e.g. CS, IT, IS, DS, SE) |
| `year` | Year level (1–4) |
| `type` | `Core` or `Lab` |
| `instructor` | Instructor ID (must match `availability.csv`) |
| `semester` | 1 or 2 |
| `enrollment` | Number of students enrolled |

### `rooms.csv`
```
id,capacity,type
R001,60,Core
L056,30,Lab
```

| Column | Description |
|---|---|
| `id` | Unique room ID |
| `capacity` | Maximum number of students |
| `type` | `Core` or `Lab` — must match course type |

### `availability.csv`
```
instructor,slot
I001,0
I001,3
I001,7
```

| Column | Description |
|---|---|
| `instructor` | Instructor ID |
| `slot` | Available time slot index (0–9) |

**Slot index reference:**

| Index | Time |
|---|---|
| 0 | Mon-Thu 8:30–10:00 |
| 1 | Mon-Thu 10:15–11:45 |
| 2 | Mon-Thu 12:00–1:30 |
| 3 | Mon-Thu 1:45–3:15 |
| 4 | Mon-Thu 3:30–5:00 |
| 5 | Tue-Fri 8:30–10:00 |
| 6 | Tue-Fri 10:15–11:45 |
| 7 | Tue-Fri 12:00–1:30 |
| 8 | Tue-Fri 1:45–3:15 |
| 9 | Tue-Fri 3:30–5:00 |

---

## How to Run

### 1. Place all files together

Make sure all `.java` files and all three `.csv` files are in the same folder:

```
mkdir scheduler
cd scheduler
# copy all .java and .csv files here
```

### 2. Compile

```bash
javac *.java
```

### 3. Run

```bash
java Main
```

### Expected Output

```
C0001 | CS | Y1 | Mon-Thu 8:30-10:00 | Room R012
C0002 | CS | Y1 | Tue-Fri 10:15-11:45 | Room L067
...
Execution time: 312 ms
```

If no valid schedule is found within 15 seconds:
```
No valid schedule found (or timeout)
Execution time: 15001 ms
```

---

## How the Scheduler Works

The scheduler uses **backtracking with constraint propagation**:

- **MRV (Minimum Remaining Values)** — always assigns the most constrained course first
- **Degree heuristic** — breaks MRV ties by choosing the course with the most conflicts
- **Least constraining value** — tries slots that leave the most room options for remaining courses
- **Forward checking** — prunes branches early if any unassigned course has zero valid moves

### Conflict Rules

Two courses conflict (cannot share a time slot) if:
1. They share the **same instructor**, or
2. They are in the **same program, same year, and both are Core** courses

### Room Assignment Rules

A room is valid for a course if:
1. The room **type matches** the course type (`Core` → lecture room, `Lab` → lab room)
2. The room **capacity ≥ enrollment**
3. The room is **not already used** in that time slot

---

## Dataset Constraints (for feasibility)

To ensure the scheduler can find a solution, your dataset must satisfy:

| Rule | Reason |
|---|---|
| Each instructor teaches ≤ 10 courses | Only 10 time slots exist |
| Each program+year group has ≤ 9 Core courses | All core courses in a group conflict with each other |
| `(number of Core rooms) × 10 ≥ total Core courses` | Enough room-slot combinations |
| `(number of Lab rooms) × 10 ≥ total Lab courses` | Enough room-slot combinations |
| Every course enrollment ≤ largest room of matching type | At least one room must fit it |

The provided `courses.csv`, `rooms.csv`, and `availability.csv` are pre-verified against all these rules and are confirmed schedulable.

---

## Troubleshooting

**`No valid schedule found (or timeout)`**
- Check that no instructor is assigned more courses than they have available slots
- Check that no program+year group has 10 or more Core courses
- Increase the number of rooms or reduce course count

**`Error parsing course at line N`**
- Make sure each row in `courses.csv` has exactly 7 comma-separated columns
- Check for extra spaces, missing values, or non-integer fields in `year`, `semester`, `enrollment`

**`Skipping invalid room row at line N`**
- Each room row needs exactly 3 columns: `id`, `capacity`, `type`

**Compile error: `cannot find symbol`**
- Make sure all six `.java` files are in the same directory before running `javac *.java`
