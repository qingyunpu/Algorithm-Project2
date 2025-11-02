import java.util.*;

/**
 * Token-level Huffman codec.
 * - Each DISTINCT token in a column (e.g., "mother", "father", "0", "5") is treated as a symbol.
 * - Builds a binary Huffman tree using a min-heap by frequency.
 * - Produces an encoding map token -> bitstring ("0"/"1").
 *
 * Note: For this project we do NOT need a full bit-level packer. We only use the bitstring
 * as a KEY to the red-black tree (deterministic mapping token -> bitstring -> RB key).
 */

/**
 * Token-level Huffman codec with tracing & ASCII tree printing for the report.
 */
public class HuffmanCodec {
    /** Tree node for Huffman building */
    static class Node implements Comparable<Node> {
        String sym;  // null for internal nodes
        int freq;
        Node left, right;
        Node(String s, int f) { sym = s; freq = f; }
        Node(Node L, Node R) { left = L; right = R; freq = L.freq + R.freq; }
        boolean isLeaf() { return sym != null; }

        public int compareTo(Node o) {
            int c = Integer.compare(this.freq, o.freq);
            if (c != 0) return c;
            // tie-breaker for stable ordering
            String a = this.sym == null ? "" : this.sym;
            String b = o.sym == null ? "" : o.sym;
            return a.compareTo(b);
        }
    }

    private final Map<String, String> enc = new HashMap<>();
    private Node root;              // keep root for ASCII printing
    private boolean traceEnabled;   // enable step-by-step merging logs

    /** Build a Huffman codec from token frequency map, with optional tracing. */
    public static HuffmanCodec fromFrequencies(Map<String, Integer> freq) {
        return fromFrequencies(freq, false);
    }

    public static HuffmanCodec fromFrequencies(Map<String, Integer> freq, boolean trace) {
        if (freq.isEmpty()) throw new IllegalArgumentException("Empty frequency map");

        PriorityQueue<Node> pq = new PriorityQueue<>();
        if (trace) {
            System.out.println("Huffman frequency table:");
            var keys = new ArrayList<>(freq.keySet());
            Collections.sort(keys);
            for (String k : keys) System.out.println("  " + k + " : " + freq.get(k));
            System.out.println();
        }
        for (Map.Entry<String, Integer> e : freq.entrySet()) {
            pq.add(new Node(e.getKey(), e.getValue()));
        }

        // Edge case: only one symbol
        if (pq.size() == 1) {
            HuffmanCodec hc = new HuffmanCodec();
            hc.traceEnabled = trace;
            Node only = pq.poll();
            hc.root = only;
            hc.enc.put(only.sym, "0");
            return hc;
        }

        int step = 1;
        while (pq.size() > 1) {
            Node a = pq.poll(), b = pq.poll();
            if (trace) {
                String la = a.isLeaf() ? ("'" + a.sym + "'") : "(internal)";
                String lb = b.isLeaf() ? ("'" + b.sym + "'") : "(internal)";
                System.out.printf("Merge #%d: %s[%d] + %s[%d] -> %d%n",
                        step++, la, a.freq, lb, b.freq, a.freq + b.freq);
            }
            pq.add(new Node(a, b));
        }

        HuffmanCodec hc = new HuffmanCodec();
        hc.traceEnabled = trace;
        hc.root = pq.poll();
        hc.build(hc.root, "");
        if (trace) System.out.println();
        return hc;
    }

    private void build(Node n, String path) {
        if (n.isLeaf()) {
            enc.put(n.sym, path.isEmpty() ? "0" : path);
        } else {
            build(n.left, path + "0");
            build(n.right, path + "1");
        }
    }

    /** Encode a token to its Huffman bitstring ("0101...") */
    public String encode(String token) {
        String bits = enc.get(token);
        if (bits == null) throw new IllegalArgumentException("Unknown token: " + token);
        return bits;
    }

    /** Print codebook sorted by token (for your report) */
    public void printCodebook() {
        ArrayList<String> keys = new ArrayList<>(enc.keySet());
        Collections.sort(keys);
        for (String k : keys) {
            System.out.println(k + " -> " + enc.get(k));
        }
    }

    /** Pretty-print the Huffman tree as ASCII with 0/1 edges. */
    public void printAsciiTree() {
        System.out.println("(Huffman tree; left=0, right=1)");
        printAscii(root, "", true, "");
    }

    private void printAscii(Node n, String prefix, boolean isTail, String edge) {
        if (n == null) return;
        String label = n.isLeaf() ? ("'" + n.sym + "'") : "(internal)";
        System.out.println(prefix + (edge.isEmpty() ? "" : edge + " ") +
                (isTail ? "└─ " : "├─ ") + label + " [" + n.freq + "]");
        String newPrefix = prefix + (isTail ? "   " : "│  ");
        if (n.left != null || n.right != null) {
            if (n.left != null && n.right != null) {
                printAscii(n.left, newPrefix, false, "0");
                printAscii(n.right, newPrefix, true, "1");
            } else if (n.left != null) {
                printAscii(n.left, newPrefix, true, "0");
            } else if (n.right != null) {
                printAscii(n.right, newPrefix, true, "1");
            }
        }
    }
}
