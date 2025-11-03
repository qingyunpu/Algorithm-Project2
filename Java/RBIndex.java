// RBIndex.java
import java.util.*;
import java.math.BigInteger;

/**
 * RBIndex bridges "Huffman(on tokens)" and your Red-Black Tree:
 * 1) Encode raw token (e.g., "mother", "0") into a Huffman bitstring "0101..."
 * 2) Map the bitstring to a stable int key that is friendly to RB-tree ordering
 * 3) Insert (key, rowId) into the RB-tree. Duplicate keys aggregate postings in the node.
 *
 * Note on mapping bitstring -> int:
 * - We prefix a leading '1' to preserve leading zeros (e.g., "001" -> "1001") then parse as binary.
 * - If the value exceeds 32-bit range (very unlikely here), we fall back to String.hashCode() deterministically.
 */
public class RBIndex {
    private final String name;
    private final HuffmanCodec codec;
    private final RedBlackTree tree = new RedBlackTree(); // use your own RB-tree implementation

    public RBIndex(String name, HuffmanCodec codec) {
        this.name = name;
        this.codec = codec;
    }

    /** Add one row's token into this index. */
    public void add(String rawToken, int rowId) {
        String bits = codec.encode(rawToken);
        int k = rbKeyFromBits(bits);
        tree.insert(k, rowId);
    }

    /** Equality lookup: encode the query token, find postings by RB key. */
    public List<Integer> findRowIds(String rawToken) {
        String bits = codec.tryEncode(rawToken);
        if (bits == null) {
            return Collections.emptyList();
        }
        int k = rbKeyFromBits(bits);
        return tree.get(k);
    }

    /** Number of distinct RB keys (i.e., distinct Huffman-encoded tokens) stored. */
    public int size() {
        return tree.sizeDistinctKeys();
    }

    /** Pretty print the RB-tree (delegates to your RedBlackTree.printTree()). */
    public void printTree() {
        tree.printTree();
    }

    /** Enable/disable verbose logging for RB-tree operations. */
    public void setVerbose(boolean v) {
        tree.setVerbose(v);
    }

    /** Enable/disable snapshots after each fix-up step. */
    public void setSnapshotAfterFixup(boolean v) {
        tree.setSnapshotAfterFixup(v);
    }

    /** Convert Huffman bitstring into an int key for the RB-tree. */
    private static int rbKeyFromBits(String bits) {
        // Keep leading zeros by adding a head '1'
        String withHead = "1" + bits;
        try {
            BigInteger bi = new BigInteger(withHead, 2);
            if (bi.bitLength() <= 31) {
                return bi.intValue();
            } else {
                // Very rare for our dataset; still deterministic within the same run/codebook
                return withHead.hashCode();
            }
        } catch (Exception e) {
            return withHead.hashCode();
        }
    }
}
