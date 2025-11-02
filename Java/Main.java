// Main.java
import java.util.*;
import java.io.*;

/**
 * Entry point for the demo:
 * - Load CSV into a memory DB (linked list + id index)
 * - Build two indexes using "Huffman(on tokens) -> Red-Black Tree"
 *   * guardian   (categorical strings)
 *   * absences   (integers turned into string tokens)
 * - Print Huffman codebooks and ASCII Huffman trees for both columns
 * - Print both RB-trees (using your RedBlackTree.printTree())
 * - Run two example equality queries for screenshots
 *
 * Usage:
 *   javac Main.java MemoryDB.java Student.java HuffmanCodec.java RedBlackTree.java RBIndex.java
 *   java Main path/to/student-data.csv
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java Main <student-data.csv>");
            return;
        }
        String csvPath = args[0];

        // 1) Load data into memory DB
        MemoryDB db = new MemoryDB();
        db.loadCsv(csvPath); // requires header with "guardian" and "absences"

        // 2) Build frequency tables for two columns
        Map<String, Integer> guardianFreq = new HashMap<>();
        Map<String, Integer> absencesFreq = new HashMap<>();
        for (Student s : db.all()) {
            guardianFreq.put(s.guardian, guardianFreq.getOrDefault(s.guardian, 0) + 1);
            String tok = String.valueOf(s.absences);
            absencesFreq.put(tok, absencesFreq.getOrDefault(tok, 0) + 1);
        }

        // 3) Build Huffman codecs WITH TRACE enabled (for step-by-step screenshots)
        HuffmanCodec guardianCodec = HuffmanCodec.fromFrequencies(guardianFreq, true);
        HuffmanCodec absencesCodec = HuffmanCodec.fromFrequencies(absencesFreq, true);

        // 4) Build RB-tree indexes using "Huffman-encoded key -> RB key (int)"
        RBIndex guardianIndex = new RBIndex("guardian", guardianCodec);
        RBIndex absencesIndex = new RBIndex("absences", absencesCodec);
        for (Student s : db.all()) {
            guardianIndex.add(s.guardian, s.id);
            absencesIndex.add(String.valueOf(s.absences), s.id);
        }

        // 5) Print codebooks
        System.out.println("\n== Guardian Huffman Codebook ==");
        guardianCodec.printCodebook();
        System.out.println("\n== Absences Huffman Codebook ==");
        absencesCodec.printCodebook();

        // 6) Print ASCII Huffman trees
        System.out.println("\n== Guardian Huffman ASCII Tree ==");
        guardianCodec.printAsciiTree();
        System.out.println("\n== Absences Huffman ASCII Tree ==");
        absencesCodec.printAsciiTree();

        // 7) Basic index stats
        System.out.println("\nGuardian index distinct keys: " + guardianIndex.size());
        System.out.println("Absences index distinct keys: " + absencesIndex.size());

        // 8) Print both RB-trees (uses your RedBlackTree.printTree())
        System.out.println("\n== Guardian RB-tree ==");
        guardianIndex.printTree();
        System.out.println("\n== Absences RB-tree ==");
        absencesIndex.printTree();

        // 9) Demo equality queries (for report screenshots)
        demoQuery("guardian=mother", guardianIndex.findRowIds("mother"), db);
        demoQuery("absences=0",     absencesIndex.findRowIds("0"), db);
    }

    /** Pretty-print a small result set by fetching records back from the memory DB. */
    private static void demoQuery(String title, List<Integer> rowIds, MemoryDB db) {
        System.out.println("\n-- Query: " + title + " --");
        if (rowIds.isEmpty()) {
            System.out.println("(no hit)");
            return;
        }
        for (int id : rowIds) {
            Student s = db.byId(id);
            if (s != null) {
                System.out.println("Row#" + s.id + "   guardian=" + s.guardian + "   absences=" + s.absences);
            }
        }
    }
}
