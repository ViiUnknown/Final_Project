import java.util.*;

public class InstructorAvailability {

    Map<String, Set<Integer>> availability = new HashMap<>();

    public void addAvailability(String instructor, int slot) {
        availability.computeIfAbsent(instructor, k -> new HashSet<>()).add(slot);
    }
    public Set<Integer> getAvailableSlots(String instructor) {
        return availability.getOrDefault(instructor, new HashSet<>());
    }

    public boolean isAvailable(String instructor, int slot) {
        return availability.getOrDefault(instructor, new HashSet<>()).contains(slot);
    }
}