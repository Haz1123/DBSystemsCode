import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Date;
import java.util.Vector;
import java.lang.Math;

public class btindex {
    public static void main(String[] args) throws IOException {
        int pageSize = Integer.parseInt(args[constants.DBQUERY_PAGE_SIZE_ARG]);
        String datafile = args[1];
        long startTime = 0;
        long finishTime = 0;
        int numBytesInOneRecord = constants.TOTAL_SIZE;
        int numRecordsPerPage = pageSize / numBytesInOneRecord;
        byte[] page = new byte[pageSize];
        FileInputStream inStream = null;

        BPlusTree tree = new BPlusTree();

        try {
            // Input file
            inStream = new FileInputStream(datafile);
            // Output setup
            FileOutputStream outputStream = new FileOutputStream("index." + pageSize);
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutput = new DataOutputStream(byteOutputStream);
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

                    // if match is found, copy bytes of other fields and print out the record
                    tree.append(ByteBuffer.wrap(birthDateBytes).getLong(), i);
                }
            }
            // tree.verifyLeafOrder();

            // Make index records.
            int totalIndexRecords = tree.root.getTreeSize();
            int recordsPerPage = Math.floorDiv(pageSize, IndexRecord.RECORD_SIZE);

            int numberOfPagesUsed = 1;

            Vector<Bucket> buckets = new Vector<Bucket>();
            int i = 0;
            buckets.add(tree.root);
            // Builds (breadth first) ordered list of all buckets
            while (i < buckets.size()) {
                buckets.get(i).bucketOrder = i;
                for (Bucket b : buckets.get(i).children) {
                    buckets.add(b);
                }
                i++;
            }
            for (i = 0; i < buckets.size(); i++) {
                Bucket tBucket = buckets.get(i);
                if (tBucket.isLeaf) { // Write leaf record
                    for (int j = 0; j < BPlusTree.MAX_BUCKET_SIZE - 1; j++) {
                        // Write node values
                        if (j < tBucket.nodes.size()) {
                            dataOutput.writeLong(tBucket.nodes.get(j).val);
                        } else {
                            dataOutput.writeLong(Long.MIN_VALUE);
                        }
                    }
                    for (int j = 0; j < BPlusTree.MAX_BUCKET_SIZE - 1; j++) {
                        // Write page numbers
                        if (j < tBucket.nodes.size()) {
                            dataOutput.writeInt(tBucket.nodes.get(j).page);
                        } else {
                            dataOutput.writeInt(-1);
                        }
                    }
                    if (tBucket.nextBucket != null) {
                        // Write page for next bucket
                        dataOutput.writeInt(Math.floorDiv(tBucket.nextBucket.bucketOrder, recordsPerPage));
                    } else {
                        dataOutput.writeInt(-1);
                    }
                    for (int j = 0; j < BPlusTree.MAX_BUCKET_SIZE - 1; j++) {
                        // Buffer in page offset section
                        dataOutput.writeInt(-1);
                    }
                    if (tBucket.nextBucket != null) {
                        dataOutput
                                .writeInt((tBucket.nextBucket.bucketOrder % recordsPerPage) * IndexRecord.RECORD_SIZE);
                    } else {
                        dataOutput.writeInt(-1);
                    }
                } else { // Write tree node record
                    for (int j = 0; j < BPlusTree.MAX_BUCKET_SIZE - 1; j++) {
                        // Node values
                        if (j < tBucket.nodes.size()) {
                            dataOutput.writeLong(tBucket.nodes.get(j).val);
                        } else {
                            dataOutput.writeLong(Long.MIN_VALUE);
                        }
                    }
                    for (int j = 0; j < BPlusTree.MAX_BUCKET_SIZE; j++) {
                        // Index page pointers
                        if (j < tBucket.children.size()) {
                            dataOutput.writeInt(Math.floorDiv(tBucket.children.get(j).bucketOrder, recordsPerPage));
                        } else {
                            dataOutput.writeInt(-1);
                        }
                    }
                    for (int j = 0; j < BPlusTree.MAX_BUCKET_SIZE; j++) {
                        // Index record offsets
                        if (j < tBucket.children.size()) {
                            dataOutput.writeInt(
                                    (tBucket.children.get(j).bucketOrder % recordsPerPage) * IndexRecord.RECORD_SIZE);
                        } else {
                            dataOutput.writeInt(-1);
                        }
                    }
                }
                if (byteOutputStream.size() % IndexRecord.RECORD_SIZE != 0) {
                    System.out.println("AAA");
                }
                // check if a new page will be needed
                if ((i + 1) % recordsPerPage == 0) {
                    dataOutput.flush();
                    // Get the byte array of loaded records, copy to an empty page and writeout
                    byte[] outPage = new byte[pageSize];
                    byte[] records = byteOutputStream.toByteArray();
                    int numberBytesToCopy = byteOutputStream.size();
                    System.arraycopy(records, 0, outPage, 0, numberBytesToCopy);
                    outputStream.write(page);
                    numberOfPagesUsed++;
                    byteOutputStream.reset();
                }
            }
            // Check if any records still need to be written
            if (byteOutputStream.size() != 0) {
                dataOutput.flush();
                byte[] outPage = new byte[pageSize];
                byte[] records = byteOutputStream.toByteArray();
                int numberBytesToCopy = byteOutputStream.size();
                System.arraycopy(records, 0, outPage, 0, numberBytesToCopy);
                outputStream.write(outPage);
                numberOfPagesUsed++;
                byteOutputStream.reset();
            }

            finishTime = System.nanoTime();
        } catch (FileNotFoundException e) {
            System.err.println("File not found " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IO Exception " + e.getMessage());
        }
        System.out.println("NanoTime to complete: " + (finishTime - startTime));
    }

    public static class IndexRecord {
        // Index Record: DATA<Long>[9] PAGE<int>[10] OFFSET<int>[10]
        // Bytes: 9*8 10*4 10*4 = 152 bytes.
        public static int RECORD_SIZE = 152;
        public long[] data;
        public int[] page;
        public int[] offset;

    }
}
