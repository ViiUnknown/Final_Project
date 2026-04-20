
public class Course {
    String id;
    String program;
    int year;
    String type;
    String instructor;
    int enrollment;

    int timeSlot = -1;
    String room = null;

    public Course(String id, String program, int year, String type, String instructor, int semester, int enrollment) {
        this.id = id;
        this.program = program;
        this.year = year;
        this.type = type;
        this.instructor = instructor;
        this.enrollment = enrollment;
    }
}