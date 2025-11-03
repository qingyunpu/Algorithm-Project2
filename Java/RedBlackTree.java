import java.util.*;

/**
 * Classic Red-Black Tree supporting:
 * - int keys
 * - duplicate keys aggregated into a postings list (List<Integer> rowIds) stored inside a single node
 *
 * Implementation notes:
 * - Uses a single NIL sentinel node to simplify null checks
 * - Insert: standard BST insert, if equal key -> append posting and RETURN (no rebalancing)
 * - If new node structurally inserted, run RB insert-fixup (cases 1/2/3) with rotations and recoloring
 * - Provides search(key) -> postings list or empty list
 */

/**
 * Red-Black Tree (int keys) with:
 * - NIL sentinel
 * - Duplicate keys aggregated into postings (List<Integer> rowIds)
 * - Standard insert + fix-up (cases 1/2/3 with rotations)
 * - sizeDistinctKeys() to count distinct keys
 * - printTree() to pretty-print as ASCII for visualization
 */
public class RedBlackTree {
    private static final boolean RED = true;
    private static final boolean BLACK = false;

    static class Node {
        int key;
        boolean color = RED;
        Node left, right, parent;
        final ArrayList<Integer> postings = new ArrayList<>();
        Node(int key) { this.key = key; }
    }

    private final Node NIL = new Node(0); // sentinel
    private Node root = NIL;
    private boolean verbose = false;
    private boolean snapshotAfterFixup = false;

    public RedBlackTree() {
        NIL.color = BLACK;
        NIL.left = NIL.right = NIL.parent = NIL;
    }

    public void setVerbose(boolean v) { this.verbose = v; }
    private void log(String s) { if (verbose) System.out.println(s); }
    public void setSnapshotAfterFixup(boolean v) { this.snapshotAfterFixup = v; }
    private void snap(String label) { if (snapshotAfterFixup) { System.out.println("[RB SNAP] " + label); printTree(); } }

    /** Insert (key,rowId). Duplicate key → append postings, no rotations needed. */
    public void insert(int key, int rowId) {
        Node y = NIL, x = root;
        while (x != NIL) {
            y = x;
            if (key == x.key) {            // duplicate → aggregate
                x.postings.add(rowId);
                log("append posting: key=" + key + " rowId=" + rowId);
                return;
            } else if (key < x.key) x = x.left;
            else                    x = x.right;
        }
        Node z = new Node(key);
        z.left = z.right = z.parent = NIL;
        z.postings.add(rowId);

        z.parent = y;
        if (y == NIL) root = z;
        else if (z.key < y.key) y.left = z;
        else                    y.right = z;

        z.color = RED;
        log("insert key=" + key + "; start fixup");
        insertFixup(z);
    }

    /** Get postings for key; empty if not found. */
    public List<Integer> get(int key) {
        Node n = searchNode(key);
        if (n == NIL) return Collections.emptyList();
        return n.postings;
    }

    /** Number of distinct keys in the tree. */
    public int sizeDistinctKeys() { return countNodes(root); }

    // ============ RB internals ============
    private Node searchNode(int key) {
        Node x = root;
        while (x != NIL) {
            if (key == x.key) return x;
            x = (key < x.key) ? x.left : x.right;
        }
        return NIL;
    }

    private int countNodes(Node n) {
        if (n == NIL) return 0;
        return 1 + countNodes(n.left) + countNodes(n.right);
    }

    private void insertFixup(Node z) {
        while (z.parent.color == RED) {
            if (z.parent == z.parent.parent.left) {
                Node y = z.parent.parent.right; // uncle
                if (y.color == RED) {
                    // Case 1
                    log("fixup case1: recolor parent/uncle, move up");
                    z.parent.color = BLACK;
                    y.color = BLACK;
                    z.parent.parent.color = RED;
                    z = z.parent.parent;
                    snap("after case1");
                } else {
                    if (z == z.parent.right) {
                        // Case 2
                        log("fixup case2: left-rotate parent");
                        z = z.parent;
                        leftRotate(z);
                        snap("after case2");
                    }
                    // Case 3
                    log("fixup case3: right-rotate grandparent");
                    z.parent.color = BLACK;
                    z.parent.parent.color = RED;
                    rightRotate(z.parent.parent);
                    snap("after case3");
                }
            } else { // mirror
                Node y = z.parent.parent.left;
                if (y.color == RED) {
                    // Case 1 mirror
                    log("fixup case1(mirror): recolor parent/uncle, move up");
                    z.parent.color = BLACK;
                    y.color = BLACK;
                    z.parent.parent.color = RED;
                    z = z.parent.parent;
                    snap("after case1(mirror)");
                } else {
                    if (z == z.parent.left) {
                        // Case 2 mirror
                        log("fixup case2(mirror): right-rotate parent");
                        z = z.parent;
                        rightRotate(z);
                        snap("after case2(mirror)");
                    }
                    // Case 3 mirror
                    log("fixup case3(mirror): left-rotate grandparent");
                    z.parent.color = BLACK;
                    z.parent.parent.color = RED;
                    leftRotate(z.parent.parent);
                    snap("after case3(mirror)");
                }
            }
        }
        root.color = BLACK;
        log("fixup done; root black");
        snap("after fixup done");
    }

    private void leftRotate(Node x) {
        Node y = x.right;
        log("leftRotate at key=" + x.key);
        x.right = y.left;
        if (y.left != NIL) y.left.parent = x;
        y.parent = x.parent;
        if (x.parent == NIL)      root = y;
        else if (x == x.parent.left) x.parent.left = y;
        else                         x.parent.right = y;
        y.left = x;
        x.parent = y;
    }

    private void rightRotate(Node x) {
        Node y = x.left;
        log("rightRotate at key=" + x.key);
        x.left = y.right;
        if (y.right != NIL) y.right.parent = x;
        y.parent = x.parent;
        if (x.parent == NIL)       root = y;
        else if (x == x.parent.right) x.parent.right = y;
        else                          x.parent.left = y;
        y.right = x;
        x.parent = y;
    }

    // ============ Visualization ============
    /** Print the tree in ASCII form: key[color], postings count. */
    public void printTree() {
        System.out.println("(RB-tree: key[color], postings)");
        printAscii(root, "", true);
    }

    private void printAscii(Node n, String prefix, boolean isTail) {
        if (n == NIL) {
            System.out.println(prefix + (isTail ? "└─ " : "├─ ") + "NIL[B]");
            return;
        }
        String color = n.color ? "R" : "B";
        System.out.println(prefix + (isTail ? "└─ " : "├─ ")
                + n.key + "[" + color + "] (postings=" + n.postings.size() + ")");
        String childPrefix = prefix + (isTail ? "   " : "│  ");
        if (n.left != NIL || n.right != NIL) {
            printAscii(n.left,  childPrefix, false);
            printAscii(n.right, childPrefix, true);
        }
    }
}