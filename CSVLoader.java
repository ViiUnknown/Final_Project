import java.io.*;
import java.util.*;

public class CSVLoader {

    // ---------------- LOAD COURSES ----------------
    public static List<Course> loadCourses(String file) throws Exception {

        List<Course> courses = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(file));

        String line = br.readLine(); // skip header
        int lineNumber = 1;

        while ((line = br.readLine()) != null) {
            lineNumber++;

            line = line.trim();

            // Skip empty lines
            if (line.isEmpty()) continue;

            String[] p = line.split(",");

            // Validate column count
            if (p.length < 7) {
                System.out.println("Skipping invalid course row at line " + lineNumber + ": " + line);
                continue;
            }

            try {
                courses.add(new Course(
                        p[0].trim(),
                        p[1].trim(),
                        Integer.parseInt(p[2].trim()),
                        p[3].trim(),
                        p[4].trim(),
                        Integer.parseInt(p[5].trim()),
                        Integer.parseInt(p[6].trim())
                ));
            } catch (Exception e) {
                System.out.println("Error parsing course at line " + lineNumber + ": " + line);
            }
        }

        br.close();
        return courses;
    }

    // ---------------- LOAD ROOMS ----------------
    public static List<Classroom> loadRooms(String file) throws Exception {

        List<Classroom> rooms = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(file));

        String line = br.readLine(); // skip header
        int lineNumber = 1;

        while ((line = br.readLine()) != null) {
            lineNumber++;

            line = line.trim();

            if (line.isEmpty()) continue;

            String[] p = line.split(",");

            if (p.length < 3) {
                System.out.println("Skipping invalid room row at line " + lineNumber + ": " + line);
                continue;
            }

            try {
                rooms.add(new Classroom(
                        p[0].trim(),
                        Integer.parseInt(p[1].trim()),
                        p[2].trim()
                ));
            } catch (Exception e) {
                System.out.println("Error parsing room at line " + lineNumber + ": " + line);
            }
        }

        br.close();
        return rooms;
    }

    // ---------------- LOAD AVAILABILITY ----------------
    public static InstructorAvailability loadAvailability(String file) throws Exception {

        InstructorAvailability availability = new InstructorAvailability();
        BufferedReader br = new BufferedReader(new FileReader(file));

        String line = br.readLine(); // skip header
        int lineNumber = 1;

        while ((line = br.readLine()) != null) {
            lineNumber++;

            line = line.trim();

            if (line.isEmpty()) continue;

            String[] p = line.split(",");

            if (p.length < 2) {
                System.out.println("Skipping invalid availability row at line " + lineNumber + ": " + line);
                continue;
            }

            try {
                availability.addAvailability(
                        p[0].trim(),
                        Integer.parseInt(p[1].trim())
                );
            } catch (Exception e) {
                System.out.println("Error parsing availability at line " + lineNumber + ": " + line);
            }
        }

        br.close();
        return availability;
    }
}