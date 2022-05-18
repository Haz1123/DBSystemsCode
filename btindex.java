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

}
