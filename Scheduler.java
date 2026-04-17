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

    Map<Integer, Set<String>> usedRooms = new HashMap<>();
    Map<Integer, Integer> slotLoad = new HashMap<>();

    long startTime;
    long TIME_LIMIT = 15000; // 15 sec

    public Scheduler(List<Course> courses, List<Classroom> rooms,
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
            slotLoad.put(i, 0);
        }

        // Sort rooms biggest first
        rooms.sort((a, b) -> b.capacity - a.capacity);

        // 🔥 FULL HEURISTIC (MRV + Degree + Enrollment)
        courses.sort((a, b) -> {

            int aSlots = availability.getAvailableSlots(a.instructor).size();
            int bSlots = availability.getAvailableSlots(b.instructor).size();

            // 1. MRV (fewer slots first)
            if (aSlots != bSlots)
                return aSlots - bSlots;

            // 2. Degree (more conflicts first)
            int degDiff = graph.get(b).size() - graph.get(a).size();
            if (degDiff != 0)
                return degDiff;

            // 3. Enrollment (larger first)
            return b.enrollment - a.enrollment;
        });
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

                if (a.program.equals(b.program)
                        && a.year == b.year
                        && a.type.equalsIgnoreCase("Core")
                        && b.type.equalsIgnoreCase("Core"))
                    conflict = true;

                if (a.instructor.equals(b.instructor))
                    conflict = true;

                if (conflict) {
                    graph.get(a).add(b);
                    graph.get(b).add(a);
                }
            }
        }
    }

    // ---------------- SOLVER ----------------
    public boolean solve() {
        return backtrack(0);
    }

    private boolean backtrack(int index) {

        if (System.currentTimeMillis() - startTime > TIME_LIMIT)
            return false;

        if (index == courses.size())
            return true;

        Course course = courses.get(index);

        List<Integer> slots =
                new ArrayList<>(availability.getAvailableSlots(course.instructor));

        // 🔥 SLOT HEURISTIC (least load + most room options)
        slots.sort((s1, s2) -> {

            int loadDiff = slotLoad.get(s1) - slotLoad.get(s2);
            if (loadDiff != 0) return loadDiff;

            int r1 = countPossibleRooms(course, s1);
            int r2 = countPossibleRooms(course, s2);

            return r2 - r1;
        });

        for (int slot : slots) {

            if (!isSlotValid(course, slot))
                continue;

            int possibleRooms = countPossibleRooms(course, slot);

            if (slotLoad.get(slot) >= possibleRooms)
                continue;

            for (Classroom room : rooms) {

                if (!isRoomValid(course, room, slot))
                    continue;

                // assign
                course.timeSlot = slot;
                course.room = room.id;

                usedRooms.get(slot).add(room.id);
                slotLoad.put(slot, slotLoad.get(slot) + 1);

                if (backtrack(index + 1))
                    return true;

                // undo
                course.timeSlot = -1;
                course.room = null;

                usedRooms.get(slot).remove(room.id);
                slotLoad.put(slot, slotLoad.get(slot) - 1);
            }
        }

        return false;
    }

    // ---------------- PRUNING ----------------
    private int countPossibleRooms(Course course, int slot) {

        int count = 0;

        for (Classroom r : rooms) {
            if (!usedRooms.get(slot).contains(r.id)
                    && r.type.equalsIgnoreCase(course.type)
                    && r.capacity >= course.enrollment) {
                count++;
            }
        }

        return count;
    }

    // ---------------- VALIDATION ----------------
    private boolean isSlotValid(Course course, int slot) {

        for (Course neighbor : graph.get(course)) {
            if (neighbor.timeSlot == slot)
                return false;
        }

        return true;
    }

    private boolean isRoomValid(Course course, Classroom room, int slot) {

        if (usedRooms.get(slot).contains(room.id))
            return false;

        if (room.capacity < course.enrollment)
            return false;

        if (!room.type.equalsIgnoreCase(course.type))
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