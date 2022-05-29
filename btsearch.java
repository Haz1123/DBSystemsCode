import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeSet;
import java.io.File;

public class btsearch {

    public static void main(String[] args) throws IOException {
        // Check for correct number of command line arguments
        if (args.length != 4) {
            System.err.println(args.length);
            System.err.println("Error: Incorrect number of arguments were input");
            return;
        }
        // Parse command line arguments
        int pageSize = Integer.parseInt(args[0].substring(args[0].indexOf(".") + 1));
        SimpleDateFormat argDateFormat = new SimpleDateFormat("yyyyMMdd");
        Date startDate = new Date();
        Date endDate = new Date();
        try {
            startDate = argDateFormat.parse(args[2]);
            endDate = argDateFormat.parse(args[3]);
        } catch (ParseException e) {
            System.err.println("Error: invalid date " + e.getMessage());
        }
        // Input setup
        FileInputStream datafileInStream = null;
        String datafilePath = args[0]; // Record heap
        String indexFilePath = args[1]; // Index
        long startTime = 0;
        long finishTime = 0;
        int numBytesInOneRecord = constants.TOTAL_SIZE;
        int numRecordsPerPage = pageSize / numBytesInOneRecord;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        byte[] datafilePage = new byte[pageSize];

        try {
            // Find target pages using the index
            byte[] indexFileBytes = Files.readAllBytes(new File(indexFilePath).toPath());

            byte[] recordBytes = new byte[IndexRecord.RECORD_SIZE];
            System.arraycopy(indexFileBytes, 0, recordBytes, 0, IndexRecord.RECORD_SIZE);
            IndexRecord targetRecord = new IndexRecord(recordBytes);
            TreeSet<Integer> targetPages = new TreeSet<Integer>();

            datafileInStream = new FileInputStream(datafilePath);

            // Create byte arrays for each field
            byte[] personNameBytes = new byte[constants.PERSON_NAME_SIZE];
            byte[] birthDateBytes = new byte[constants.BIRTH_DATE_SIZE];
            byte[] birthPlaceBytes = new byte[constants.BIRTH_PLACE_SIZE];
            byte[] deathDateBytes = new byte[constants.DEATH_DATE_SIZE];
            byte[] fieldBytes = new byte[constants.FIELD_SIZE];
            byte[] genreBytes = new byte[constants.GENRE_SIZE];
            byte[] instrumentBytes = new byte[constants.INSTRUMENT_SIZE];
            byte[] nationalityBytes = new byte[constants.NATIONALITY_SIZE];
            byte[] thumbnailBytes = new byte[constants.THUMBNAIL_SIZE];
            byte[] wikipageIdBytes = new byte[constants.WIKIPAGE_ID_SIZE];
            byte[] descriptionBytes = new byte[constants.DESCRIPTION_SIZE];

            startTime = System.nanoTime();

            boolean startLocated = false;
            // Find the first record in the range.
            while (!startLocated) {
                if (!targetRecord.isLeaf()) {
                    int nextRecordPage = targetRecord.page[targetRecord.page.length - 1]; // Default to last child
                    int nextRecordOffset = targetRecord.offset[targetRecord.offset.length - 1];
                    boolean nextRecordFound = false;
                    for (int i = 0; i < targetRecord.data.length && !nextRecordFound; i++) {
                        Date targetDate = new Date(targetRecord.data[i]);
                        if (targetRecord.data[i] == Long.MIN_VALUE || !startDate.after(targetDate)) {
                            nextRecordPage = targetRecord.page[i];
                            nextRecordOffset = targetRecord.offset[i];
                            nextRecordFound = true;
                        }
                    }
                    System.arraycopy(indexFileBytes, (nextRecordPage * pageSize) + nextRecordOffset, recordBytes, 0,
                            IndexRecord.RECORD_SIZE);
                    targetRecord = new IndexRecord(recordBytes);
                } else {
                    startLocated = true;
                }
            }
            boolean continueSearch = true;
            while (continueSearch) {
                for (int i = 0; i < targetRecord.data.length && targetRecord.data[i] != Long.MIN_VALUE; i++) {
                    Date targetDate = new Date(targetRecord.data[i]);
                    // System.out.println(targetRecord.page[i] + ":" + targetDate.toString());
                    if (!startDate.after(targetDate) && !endDate.before(targetDate)) {
                        targetPages.add(targetRecord.page[i]);
                    } else if (targetDate.after(endDate)) {
                        continueSearch = false;
                    }
                }
                if (targetRecord.page[targetRecord.page.length - 1] == -1) {
                    // End of available buckets
                    continueSearch = false;
                } else {
                    // Move to next bucket
                    System.arraycopy(indexFileBytes,
                            (targetRecord.page[targetRecord.page.length - 1] * pageSize
                                    + targetRecord.offset[targetRecord.offset.length - 1]),
                            recordBytes, 0, IndexRecord.RECORD_SIZE);
                    targetRecord = new IndexRecord(recordBytes);
                }

            }

            Integer[] relevantPages = targetPages.toArray(new Integer[0]);
            int lastPage = 0;
            datafileInStream.read(datafilePage);
            for (int pageIndex = 0; pageIndex < relevantPages.length; pageIndex++) {
                // Read in the number of pages in between the last page and this page
                if ((relevantPages[pageIndex] - lastPage) - 1 >= 1) {
                    datafileInStream.skip(((relevantPages[pageIndex] - lastPage) - 1) * pageSize);
                }
                datafileInStream.read(datafilePage);

                // Check records in page (using example code)
                for (int i = 0; i < numRecordsPerPage; i++) {

                    // Copy record's person name and birth date
                    System.arraycopy(datafilePage, ((i * numBytesInOneRecord) + constants.PERSON_NAME_OFFSET),
                            personNameBytes,
                            0, constants.PERSON_NAME_SIZE);
                    System.arraycopy(datafilePage, ((i * numBytesInOneRecord) + constants.BIRTH_DATE_OFFSET),
                            birthDateBytes, 0,
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
                    if (!startDate.after(birthDate) && !endDate.before(birthDate)) {
                        /*
                         * Copy the corresponding sections of "page" to the individual field byte arrays
                         */
                        System.arraycopy(datafilePage, ((i * numBytesInOneRecord) + constants.BIRTH_PLACE_OFFSET),
                                birthPlaceBytes, 0, constants.BIRTH_PLACE_SIZE);
                        System.arraycopy(datafilePage, ((i * numBytesInOneRecord) + constants.DEATH_DATE_OFFSET),
                                deathDateBytes, 0, constants.DEATH_DATE_SIZE);
                        System.arraycopy(datafilePage, ((i * numBytesInOneRecord) + constants.FIELD_OFFSET), fieldBytes,
                                0,
                                constants.FIELD_SIZE);
                        System.arraycopy(datafilePage, ((i * numBytesInOneRecord) + constants.GENRE_OFFSET), genreBytes,
                                0,
                                constants.GENRE_SIZE);
                        System.arraycopy(datafilePage, ((i * numBytesInOneRecord) + constants.INSTRUMENT_OFFSET),
                                instrumentBytes, 0, constants.INSTRUMENT_SIZE);
                        System.arraycopy(datafilePage, ((i * numBytesInOneRecord) + constants.NATIONALITY_OFFSET),
                                nationalityBytes, 0, constants.NATIONALITY_SIZE);
                        System.arraycopy(datafilePage, ((i * numBytesInOneRecord) + constants.THUMBNAIL_OFFSET),
                                thumbnailBytes,
                                0, constants.THUMBNAIL_SIZE);
                        System.arraycopy(datafilePage, ((i * numBytesInOneRecord) + constants.WIKIPAGE_ID_OFFSET),
                                wikipageIdBytes, 0, constants.WIKIPAGE_ID_SIZE);
                        System.arraycopy(datafilePage, ((i * numBytesInOneRecord) + constants.DESCRIPTION_OFFSET),
                                descriptionBytes, 0, constants.DESCRIPTION_SIZE);

                        // Convert long data into Date object
                        long deathDateLong = ByteBuffer.wrap(deathDateBytes).getLong();
                        String deathDateStr = "NULL";
                        if (0 != deathDateLong) {
                            Date deathDate = new Date(ByteBuffer.wrap(deathDateBytes).getLong());
                            deathDateStr = dateFormat.format(deathDate);
                        }

                        // Get a string representation of the record for printing to stdout
                        String record = new String(personNameBytes).trim() + ","
                                + dateFormat.format(birthDate) + ","
                                + new String(birthPlaceBytes).trim() + ","
                                + deathDateStr + ","
                                + new String(fieldBytes).trim() + ","
                                + new String(genreBytes).trim() + ","
                                + new String(instrumentBytes).trim() + ","
                                + new String(nationalityBytes).trim() + ","
                                + new String(thumbnailBytes).trim() + ","
                                + ByteBuffer.wrap(wikipageIdBytes).getInt() + ","
                                + new String(descriptionBytes).trim();
                        System.out.println(record);
                    } else {
                        System.err.println("False flag?");
                    }
                }

                lastPage = relevantPages[pageIndex];
            }

            finishTime = System.nanoTime();

        } catch (FileNotFoundException e) {
            System.err.println("File not found " + e.getMessage());
        } finally {

        }
        long timeInMilliseconds = (finishTime - startTime) / constants.MILLISECONDS_PER_SECOND;
        System.out.println("Time taken: " + timeInMilliseconds + " ms");
    }
}
