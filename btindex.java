import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Vector;
import java.util.stream.Collectors;

public class btindex {
    public static void main(String[] args) throws IOException {
        int pageSize = Integer.parseInt(args[constants.DBQUERY_PAGE_SIZE_ARG]);
        SimpleDateFormat argDateFormat = new SimpleDateFormat("yyyyMMdd");

        String datafile = "heap." + pageSize;
        long startTime = 0;
        long finishTime = 0;
        int numBytesInOneRecord = constants.TOTAL_SIZE;
        int numBytesIntField = Integer.BYTES;
        int numRecordsPerPage = pageSize / numBytesInOneRecord;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        byte[] page = new byte[pageSize];
        FileInputStream inStream = null;

        BPlusTree tree = new BPlusTree();

        try {
            inStream = new FileInputStream(datafile);
            int numBytesRead = 0;
            startTime = System.nanoTime();
            // Create byte arrays for each field
            byte[] personNameBytes = new byte[constants.PERSON_NAME_SIZE];
            byte[] birthDateBytes = new byte[constants.BIRTH_DATE_SIZE];

            // until the end of the binary file is reached
            while ((numBytesRead = inStream.read(page)) != -1) {
                // Process each record in page
                for (int i = 0; i < numRecordsPerPage; i++) {

                    // Copy record's person name and birth date
                    System.arraycopy(page, ((i * numBytesInOneRecord) + constants.PERSON_NAME_OFFSET), personNameBytes,
                            0, constants.PERSON_NAME_SIZE);
                    System.arraycopy(page, ((i * numBytesInOneRecord) + constants.BIRTH_DATE_OFFSET), birthDateBytes, 0,
                            constants.BIRTH_DATE_SIZE);

                    // Check if person name field is empty; if so, end of all records found (packed
                    // organisation)
                    if (personNameBytes[0] == 0) {
                        // can stop checking records
                        break;
                    }

                    // Check for match
                    long birthDateLong = ByteBuffer.wrap(birthDateBytes).getLong();
                    if (0 == birthDateLong) {
                        // skip NULL birth dates
                        continue;
                    }
                    Date birthDate = new Date(ByteBuffer.wrap(birthDateBytes).getLong());

                    // if match is found, copy bytes of other fields and print out the record
                    tree.append(ByteBuffer.wrap(birthDateBytes).getLong(), i);
                }
            }

            finishTime = System.nanoTime();
            tree.verifyLeafOrder();
        } catch (FileNotFoundException e) {
            System.err.println("File not found " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO Exception " + e.getMessage());
        } finally {
            if (inStream != null) {
                inStream.close();
            }
        }

    }

    private static class BPlusTree {
        public Bucket root;
        public int maxBucketSize = 10;

        public BPlusTree() {
            root = new Bucket(null, maxBucketSize);
            root.isLeaf = true;
        }

        public void append(long birthDate, int pageNum) {
            this.root.insert(new Node(birthDate, pageNum));
        }

        public void verifyLeafOrder() {
            Bucket targetBucket = this.root;
            while (targetBucket.isLeaf == false) {
                targetBucket = targetBucket.children.firstElement();
            }
            while (targetBucket != null) {
                System.out.println(targetBucket.nodes.firstElement().val);
                targetBucket = targetBucket.nextBucket;
            }
        }

        public Bucket findBucket(long birthDate) {
            Bucket target = root;
            while (target.children.size() != 0) {
                boolean found = false;
                for (int i = 0; i < target.children.size(); i++) {
                    if (birthDate < target.nodes.get(i).val) {
                        found = true;
                        target = target.children.get(i);
                    }
                    if (!found) {
                        target = target.children.lastElement();
                    }
                }
            }
            return target;
        }

    }

    private static class Node implements Comparable<Node> {
        public long val;
        public int page;

        public Node(long val, int page) {
            this.val = val;
            this.page = page;
        }

        @Override
        public int compareTo(btindex.Node o) {
            if (this.val == o.val) {
                return 0;
            } else if (this.val < o.val) {
                return -1;
            } else {
                return 1;
            }
        }

    }

    private static class Bucket {
        public Bucket parent;
        public Vector<Bucket> children;
        public Vector<Node> nodes;
        public int maxSize;
        public boolean isLeaf;
        public Bucket nextBucket;

        public Bucket(Bucket parent, int maxSize) {
            this(parent, maxSize, false);
        }

        public Bucket(Bucket parent, int maxSize, boolean isLeaf) {
            this.parent = parent;
            this.children = new Vector<Bucket>();
            this.nodes = new Vector<Node>();
            this.maxSize = maxSize;
            this.isLeaf = isLeaf;
        }

        public String getStringRep(int dep) {
            String out = "{";
            out += ("\"Nodes\":[");
            out += this.nodes.stream().map(s -> (Long.toString(s.val))).collect(Collectors.joining(","));
            out += "],";
            out += "\"Children\":[";
            if (dep > 0) {
                out += this.children.stream().map(c -> (c.getStringRep(dep - 1))).collect(Collectors.joining(","));
            }
            out += "]}";
            return out;
        }

        public boolean verifyChildren() {
            boolean out = true;
            for (Bucket bucket : children) {
                out = out && bucket.parent == this;
            }
            return out;
        }

        public Node insert(Node node) {
            if (this.isLeaf) {
                // This is a leaf bucket
                this.nodes.add(node);
                this.nodes.sort(null);
                if (this.nodes.size() >= this.maxSize - 1) { // Need to split
                    Bucket left = new Bucket(null, this.maxSize, true);
                    for (int i = 0; i < this.nodes.size() / 2; i++) {
                        left.nodes.add(this.nodes.get(i));
                    }
                    Bucket right = new Bucket(null, this.maxSize, true);
                    for (int i = this.nodes.size() / 2; i < this.nodes.size(); i++) {
                        right.nodes.add(this.nodes.get(i));
                    }
                    if (this.parent == null) {
                        // First leaf bucket to be split.
                        // Make this bucket into the root
                        left.parent = this;
                        right.parent = this;
                        this.nodes.clear();
                        this.nodes.add(right.nodes.firstElement());
                        this.children.add(left);
                        left.nextBucket = right;
                        this.children.add(right);
                        this.isLeaf = false;
                    } else {
                        right.parent = this.parent;
                        right.nextBucket = this.nextBucket;
                        this.nextBucket = right;
                        this.nodes.clear();
                        for (Node x : left.nodes) {
                            this.nodes.add(x);
                        }
                        int selfIndex = this.parent.children.indexOf(this);
                        this.parent.nodes.add(selfIndex, right.nodes.firstElement());
                        this.parent.children.add(selfIndex + 1, right);
                    }
                }
            } else {
                boolean found = false;
                Bucket target = this.children.lastElement(); // Default to last bucket
                for (int i = 0; i < this.nodes.size() && !found; i++) {
                    if (node.val < this.nodes.get(i).val) {
                        found = true;
                        target = this.children.get(i);
                    }
                }
                target.insert(node);
                if (this.nodes.size() >= this.maxSize - 1) {
                    // This bucket needs to be split.
                    Bucket left = new Bucket(this, maxSize);
                    Bucket right = new Bucket(this, maxSize);
                    for (int i = 0; i < this.nodes.size() / 2; i++) {
                        left.nodes.add(this.nodes.get(i));
                        left.children.add(this.children.get(i));
                    }
                    left.children.add(this.children.get((int) this.nodes.size() / 2));
                    left.children.forEach(c -> c.parent = left);
                    for (int i = this.nodes.size() / 2; i < this.nodes.size(); i++) {
                        right.nodes.add(this.nodes.get(i));
                        right.children.add(this.children.get(i + 1));
                        this.children.get(i + 1).parent = right;
                    }
                    Node pushUp = right.nodes.firstElement();
                    right.nodes.remove(pushUp);
                    if (this.parent == null) {
                        // This bucket is the root node.
                        this.nodes.clear();
                        this.children.clear();
                        this.nodes.add(pushUp);
                        this.children.add(left);
                        this.children.add(right);
                    } else {
                        // This bucket is on the tree
                        this.nodes.clear();
                        this.children.clear();
                        this.nodes.addAll(left.nodes);
                        this.children.addAll(left.children);
                        this.children.forEach(c -> c.parent = this);
                        int selfIndex = this.parent.children.indexOf(this);
                        right.parent = this.parent;
                        this.parent.nodes.add(selfIndex, pushUp);
                        this.parent.children.add(selfIndex + 1, right);
                    }
                }
            }
            if (!this.verifyChildren()) {
                System.out.println(";");
            }
            return null;
        }

    }
}
