import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {

        List<Course> courses =
                CSVLoader.loadCourses("courses.csv");

        List<Classroom> rooms =
                CSVLoader.loadRooms("rooms.csv");

        InstructorAvailability availability =
                CSVLoader.loadAvailability("availability.csv");

        Scheduler scheduler =
                new Scheduler(courses, rooms, availability);

        long start = System.currentTimeMillis();

        if (scheduler.solve()) {
            scheduler.printSchedule();
        } else {
            System.out.println("No valid schedule found (or timeout)");
        }

        long end = System.currentTimeMillis();
        System.out.println("Execution time: " + (end - start) + " ms");
    }
}