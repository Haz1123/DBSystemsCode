import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

public class dbquery {

    // Reads in a binary file of the argument-specified pagesize, prints out matching records
    public static void main(String[] args) throws IOException {

        // check for correct number of arguments
        if (args.length != constants.DBQUERY_ARG_COUNT) {
            System.err.println("Error: Incorrect number of arguments were input");
            return;
        }

        int pageSize = Integer.parseInt(args[constants.DBQUERY_PAGE_SIZE_ARG]);
        SimpleDateFormat argDateFormat = new SimpleDateFormat("yyyyMMdd");
        long startDateLong = 0;
        long endDateLong = 0;
        Date startDate = new Date();
        Date endDate = new Date();
        try {
          startDate = argDateFormat.parse(args[constants.DBQUERY_START_DATE_ARG]);
          endDate = argDateFormat.parse(args[constants.DBQUERY_END_DATE_ARG]);
          startDateLong = startDate.getTime();
          endDateLong = endDate.getTime();
        } catch (ParseException e) {
            System.err.println("Error: invalid date " + e.getMessage());
        }

        String datafile = "heap." + pageSize;
        long startTime = 0;
        long finishTime = 0;
        int numBytesInOneRecord = constants.TOTAL_SIZE;
        int numBytesIntField = Integer.BYTES;
        int numRecordsPerPage = pageSize/numBytesInOneRecord;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        byte[] page = new byte[pageSize];
        FileInputStream inStream = null;

        try {
            inStream = new FileInputStream(datafile);
            int numBytesRead = 0;
            startTime = System.nanoTime();
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

            // until the end of the binary file is reached
            while ((numBytesRead = inStream.read(page)) != -1) {
                // Process each record in page
                for (int i = 0; i < numRecordsPerPage; i++) {

                    // Copy record's person name and birth date
                    System.arraycopy(page, ((i*numBytesInOneRecord) + constants.PERSON_NAME_OFFSET), personNameBytes, 0, constants.PERSON_NAME_SIZE);
                    System.arraycopy(page, ((i*numBytesInOneRecord) + constants.BIRTH_DATE_OFFSET), birthDateBytes, 0, constants.BIRTH_DATE_SIZE);

                    // Check if person name field is empty; if so, end of all records found (packed organisation)
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
                        System.arraycopy(page, ((i*numBytesInOneRecord) + constants.BIRTH_PLACE_OFFSET), birthPlaceBytes, 0, constants.BIRTH_PLACE_SIZE);
                        System.arraycopy(page, ((i*numBytesInOneRecord) + constants.DEATH_DATE_OFFSET), deathDateBytes, 0, constants.DEATH_DATE_SIZE);
                        System.arraycopy(page, ((i*numBytesInOneRecord) + constants.FIELD_OFFSET), fieldBytes, 0, constants.FIELD_SIZE);
                        System.arraycopy(page, ((i*numBytesInOneRecord) + constants.GENRE_OFFSET), genreBytes, 0, constants.GENRE_SIZE);
                        System.arraycopy(page, ((i*numBytesInOneRecord) + constants.INSTRUMENT_OFFSET), instrumentBytes, 0, constants.INSTRUMENT_SIZE);
                        System.arraycopy(page, ((i*numBytesInOneRecord) + constants.NATIONALITY_OFFSET), nationalityBytes, 0, constants.NATIONALITY_SIZE);
                        System.arraycopy(page, ((i*numBytesInOneRecord) + constants.THUMBNAIL_OFFSET), thumbnailBytes, 0, constants.THUMBNAIL_SIZE);
                        System.arraycopy(page, ((i*numBytesInOneRecord) + constants.WIKIPAGE_ID_OFFSET), wikipageIdBytes, 0, constants.WIKIPAGE_ID_SIZE);
                        System.arraycopy(page, ((i*numBytesInOneRecord) + constants.DESCRIPTION_OFFSET), descriptionBytes, 0, constants.DESCRIPTION_SIZE);

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
                    }
                }
            }

            finishTime = System.nanoTime();
        }
        catch (FileNotFoundException e) {
            System.err.println("File not found " + e.getMessage());
        }
        catch (IOException e) {
            System.err.println("IO Exception " + e.getMessage());
        }
        finally {

            if (inStream != null) {
                inStream.close();
            }
        }

        long timeInMilliseconds = (finishTime - startTime)/constants.MILLISECONDS_PER_SECOND;
        System.out.println("Time taken: " + timeInMilliseconds + " ms");
    }
}
