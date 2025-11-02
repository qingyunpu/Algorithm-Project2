// MemoryDB.java
import java.io.*;
import java.util.*;

/**
 * Minimal "memory database" for the project:
 * - Stores all rows in a singly linked list (to match assignment spec)
 * - Also keeps an ArrayList index (id -> Student) for O(1) fetch by id during demo
 * - Loads CSV and extracts at least the two indexed columns: guardian, absences
 */
public class MemoryDB {
    static class Node {
        Student v;
        Node next;
        Node(Student v) { this.v = v; }
    }

    private Node head, tail;
    private int size = 0;

    // Optional: id -> Student direct index for fast retrieval
    private final ArrayList<Student> idIndex = new ArrayList<>();

    public void append(Student s) {
        Node n = new Node(s);
        if (tail == null) { head = tail = n; }
        else { tail.next = n; tail = n; }
        size++;
        // maintain id index (ensures index size grows to fit id)
        while (idIndex.size() <= s.id) idIndex.add(null);
        idIndex.set(s.id, s);
    }

    public int size() { return size; }

    public Iterable<Student> all() {
        return () -> new Iterator<Student>() {
            Node cur = head;
            public boolean hasNext() { return cur != null; }
            public Student next() { Student x = cur.v; cur = cur.next; return x; }
        };
    }

    public Student byId(int id) {
        if (id < 0 || id >= idIndex.size()) return null;
        return idIndex.get(id);
    }

    public void loadCsv(String path) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String header = br.readLine();
            if (header == null) throw new RuntimeException("Empty CSV");
            String[] cols = splitCsv(header);

            int idxGuardian = indexOf(cols, "guardian");
            int idxAbsences = indexOf(cols, "absences");
            if (idxGuardian < 0 || idxAbsences < 0) {
                throw new RuntimeException("CSV header must contain 'guardian' and 'absences'. Found: " + String.join(",", cols));
            }

            String line;
            int rowId = 0;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] f = splitCsv(line);

                // Defensive: ensure array bounds
                if (idxGuardian >= f.length || idxAbsences >= f.length) continue;

                String guardian = f[idxGuardian].trim();
                int absences = parseIntSafe(f[idxAbsences].trim(), 0);

                // Keep whole row optionally (for reporting)
                append(new Student(rowId++, guardian, absences, f));
            }
        }
    }

    private static int indexOf(String[] arr, String target) {
        for (int i = 0; i < arr.length; i++) if (arr[i].trim().equals(target)) return i;
        return -1;
    }

    private static int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    private static String[] splitCsv(String line) {
        // This dataset is simple (no quoted commas). For general CSVs, use a proper parser.
        return line.split(",");
    }
}
