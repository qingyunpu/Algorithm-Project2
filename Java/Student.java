
/**
 * Minimal student record keeping only the two indexed columns and the raw row.
 */
public class Student {
    public final int id;
    public final String guardian;
    public final int absences;
    public final String[] raw;

    public Student(int id, String guardian, int absences, String[] raw) {
        this.id = id;
        this.guardian = guardian;
        this.absences = absences;
        this.raw = raw;
    }
}
