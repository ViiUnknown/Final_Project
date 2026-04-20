import java.util.*;

public class Scheduler {

    List<Course> courses;
    List<Classroom> rooms;
    InstructorAvailability availability;

    Map<Course, List<Course>> graph = new HashMap<>();

    int TOTAL_SLOTS = 10;

    static String[] TIME_SLOTS = {
            "Mon-Thu 8:30-10:00",
            "Mon-Thu 10:15-11:45",
            "Mon-Thu 12:00-1:30",
            "Mon-Thu 1:45-3:15",
            "Mon-Thu 3:30-5:00",
            "Tue-Fri 8:30-10:00",
            "Tue-Fri 10:15-11:45",
            "Tue-Fri 12:00-1:30",
            "Tue-Fri 1:45-3:15",
            "Tue-Fri 3:30-5:00"
    };

    // slot -> used room ids
    Map<Integer, Set<String>> usedRooms = new HashMap<>();

    long startTime;
    long TIME_LIMIT = 15000; // 15 seconds

    public Scheduler(List<Course> courses,
                     List<Classroom> rooms,
                     InstructorAvailability availability) {

        this.courses = courses;
        this.rooms = rooms;
        this.availability = availability;

        buildGraph();
        prepare();

        startTime = System.currentTimeMillis();
    }

    // ---------------- PREP ----------------
    private void prepare() {

        for (int i = 0; i < TOTAL_SLOTS; i++) {
            usedRooms.put(i, new HashSet<>());
        }

        // sort rooms biggest first
        rooms.sort((a, b) -> b.capacity - a.capacity);
    }

    // ---------------- GRAPH ----------------
    private void buildGraph() {

        for (Course c : courses)
            graph.put(c, new ArrayList<>());

        for (int i = 0; i < courses.size(); i++) {
            for (int j = i + 1; j < courses.size(); j++) {

                Course a = courses.get(i);
                Course b = courses.get(j);

                boolean conflict = false;

                // same program + same year + both core
                if (a.program.equals(b.program)
                        && a.year == b.year
                        && a.type.equalsIgnoreCase("Core")
                        && b.type.equalsIgnoreCase("Core")) {
                    conflict = true;
                }

                // same instructor
                if (a.instructor.equals(b.instructor)) {
                    conflict = true;
                }

                if (conflict) {
                    graph.get(a).add(b);
                    graph.get(b).add(a);
                }
            }
        }
    }

    // ---------------- SOLVER ----------------
    public boolean solve() {
        return backtrack();
    }

    private boolean backtrack() {

        if (System.currentTimeMillis() - startTime > TIME_LIMIT)
            return false;

        Course course = selectNextCourse();

        if (course == null)
            return true; // all assigned

        List<Integer> slots =
                new ArrayList<>(availability.getAvailableSlots(course.instructor));

        // Least constraining slot heuristic
        slots.sort((s1, s2) -> {
            int c1 = countPossibleRooms(course, s1);
            int c2 = countPossibleRooms(course, s2);
            return c2 - c1; // more room choices first
        });

        for (int slot : slots) {

            if (!isSlotValid(course, slot))
                continue;

            List<Classroom> candidates = getValidRooms(course, slot);

            for (Classroom room : candidates) {

                // assign
                course.timeSlot = slot;
                course.room = room.id;
                usedRooms.get(slot).add(room.id);

                // forward checking
                if (forwardCheck() && backtrack())
                    return true;

                // undo
                usedRooms.get(slot).remove(room.id);
                course.timeSlot = -1;
                course.room = null;
            }
        }

        return false;
    }

    // ---------------- DYNAMIC MRV ----------------
    private Course selectNextCourse() {

        Course best = null;
        int bestDomain = Integer.MAX_VALUE;
        int bestDegree = -1;
        int bestEnroll = -1;

        for (Course c : courses) {

            if (c.timeSlot != -1)
                continue;

            int domain = countLegalMoves(c);
            int degree = graph.get(c).size();

            if (domain < bestDomain ||
               (domain == bestDomain && degree > bestDegree) ||
               (domain == bestDomain && degree == bestDegree
                        && c.enrollment > bestEnroll)) {

                best = c;
                bestDomain = domain;
                bestDegree = degree;
                bestEnroll = c.enrollment;
            }
        }

        return best;
    }

    // ---------------- FORWARD CHECK ----------------
    private boolean forwardCheck() {

        for (Course c : courses) {

            if (c.timeSlot != -1)
                continue;

            if (countLegalMoves(c) == 0)
                return false;
        }

        return true;
    }

    // ---------------- DOMAIN SIZE ----------------
    private int countLegalMoves(Course c) {

        int count = 0;

        for (int slot : availability.getAvailableSlots(c.instructor)) {

            if (!isSlotValid(c, slot))
                continue;

            count += countPossibleRooms(c, slot);

            if (count > 0)
                return count; // enough to know possible
        }

        return count;
    }

    // ---------------- ROOM HELPERS ----------------
    private int countPossibleRooms(Course course, int slot) {

        int count = 0;

        for (Classroom room : rooms) {
            if (isRoomValid(course, room, slot))
                count++;
        }

        return count;
    }

    private List<Classroom> getValidRooms(Course course, int slot) {

        List<Classroom> list = new ArrayList<>();

        for (Classroom room : rooms) {
            if (isRoomValid(course, room, slot))
                list.add(room);
        }

        return list;
    }

    // ---------------- VALIDATION ----------------
    private boolean isSlotValid(Course course, int slot) {

        for (Course neighbor : graph.get(course)) {
            if (neighbor.timeSlot == slot)
                return false;
        }

        return true;
    }

    private boolean isRoomValid(Course course,
                                Classroom room,
                                int slot) {

        if (usedRooms.get(slot).contains(room.id))
            return false;

        if (!room.type.equalsIgnoreCase(course.type))
            return false;

        if (room.capacity < course.enrollment)
            return false;

        return true;
    }

    // ---------------- OUTPUT ----------------
    public void printSchedule() {

        for (Course c : courses) {
            System.out.println(
                    c.id + " | " +
                    c.program + " | Y" + c.year +
                    " | " + TIME_SLOTS[c.timeSlot] +
                    " | Room " + c.room
            );
        }
    }
}