import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Vector;

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
                    System.out.println(ByteBuffer.wrap(birthDateBytes).getLong());
                    tree.append(ByteBuffer.wrap(birthDateBytes).getLong(), i);
                }
            }

            finishTime = System.nanoTime();
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
        }

        public void append(long birthDate, int pageNum) {
            Bucket targetBucket = findBucket(birthDate);
            targetBucket.nodes.add(new Node(birthDate, pageNum));
            targetBucket.nodes.sort(null);
        }

        public Bucket findBucket(long birthDate) {
            Bucket target = root;
            while (target.children.size() == 0) {
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

        public Bucket(Bucket parent, int maxSize) {
            this.parent = parent;
            this.children = new Vector<Bucket>();
            this.nodes = new Vector<Node>();
            this.maxSize = maxSize;
        }
    }
}
