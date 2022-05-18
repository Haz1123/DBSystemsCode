import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class dbload {
    /*
     * Loads data from an input CSV into fixed-length records. Record fields are:
     * personname = 70 bytes
     * birthdate = 8 bytes
     * birthPlace_label = 354 bytes
     * deathDate 8 bytes
     * field_label 242 bytes
     * genre_label 386 bytes
     * instrument_abel 541 bytes
     * nationality_label 119 bytes
     * thumbnail 292 bytes
     * wikiPageID 4 bytes
     * description 466 bytes
     *
     * Outputs a binary file called heap.pagesize
     */
    public static void main(String[] args) throws IOException {

        // check for correct number of arguments
        if (args.length != constants.DBLOAD_ARG_COUNT) {
            System.out.println("Error: Incorrect number of arguments were input");
            return;
        }

        int pageSize = Integer.parseInt(args[constants.DBLOAD_PAGE_SIZE_ARG]);
        String datafile = args[constants.DATAFILE_ARG];
        String outputFileName = "heap." + pageSize;
        int numRecordsLoaded = 0;
        int numberOfPagesUsed = 0;
        long startTime = 0;
        long finishTime = 0;
        boolean exceptionOccurred = false;
        final int numBytesFixedLengthRecord = constants.TOTAL_SIZE;
        int numRecordsPerPage = pageSize/numBytesFixedLengthRecord;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        BufferedReader reader = null;
        FileOutputStream outputStream = null;
        ByteArrayOutputStream byteOutputStream = null;
        DataOutputStream dataOutput = null;

        try {
            reader = new BufferedReader(new FileReader(datafile));
            outputStream = new FileOutputStream(outputFileName);
            byteOutputStream = new ByteArrayOutputStream();
            dataOutput = new DataOutputStream(byteOutputStream);

            startTime = System.nanoTime();

            // read in the header line (not processed further, as datafile fieldnames are known)
            String line = reader.readLine();

            // read in lines while not the end of file
            while ((line = reader.readLine()) != null) {

                String[] valuesAsStrings = line.split(",");

                // Convert data into relevant data types
                String name = valuesAsStrings[constants.PERSON_NAME_POS];
                String birthDateStr = valuesAsStrings[constants.BIRTH_DATE_POS];
                String birthPlace = valuesAsStrings[constants.BIRTH_PLACE_POS];
                String deathDateStr = valuesAsStrings[constants.DEATH_DATE_POS];
                String field = valuesAsStrings[constants.FIELD_POS];
                String genre = valuesAsStrings[constants.GENRE_POS];
                String instrument = valuesAsStrings[constants.INSTRUMENT_POS];
                String nationality = valuesAsStrings[constants.NATIONALITY_POS];
                String thumbnail = valuesAsStrings[constants.THUMBNAIL_POS];
                int wikipageId = Integer.parseInt(valuesAsStrings[constants.WIKIPAGE_ID_POS]);
                String description = valuesAsStrings[constants.DESCRIPTION_POS];

                // parse datetime field into a date object, then get long datatype representation
                long birthDateLong = 0;
                if (!birthDateStr.equals("NULL")) {
                  Date birthDate = dateFormat.parse(birthDateStr);
                  birthDateLong = birthDate.getTime();
                }
                long deathDateLong = 0;
                if (!deathDateStr.equals("NULL")) {
                  Date deathDate = dateFormat.parse(deathDateStr);
                  deathDateLong = deathDate.getTime();
                }

                // Write bytes to data output stream
                dataOutput.writeBytes(getStringOfLength(name, constants.PERSON_NAME_SIZE));
                dataOutput.writeLong(birthDateLong);
                dataOutput.writeBytes(getStringOfLength(birthPlace, constants.BIRTH_PLACE_SIZE));
                dataOutput.writeLong(deathDateLong);
                dataOutput.writeBytes(getStringOfLength(field, constants.FIELD_SIZE));
                dataOutput.writeBytes(getStringOfLength(genre, constants.GENRE_SIZE));
                dataOutput.writeBytes(getStringOfLength(instrument, constants.INSTRUMENT_SIZE));
                dataOutput.writeBytes(getStringOfLength(nationality, constants.NATIONALITY_SIZE));
                dataOutput.writeBytes(getStringOfLength(thumbnail, constants.THUMBNAIL_SIZE));
                dataOutput.writeInt(wikipageId);
                dataOutput.writeBytes(getStringOfLength(description, constants.DESCRIPTION_SIZE));

                numRecordsLoaded++;
                // check if a new page is needed
                if (numRecordsLoaded % numRecordsPerPage == 0) {
                    dataOutput.flush();
                    // Get the byte array of loaded records, copy to an empty page and writeout
                    byte[] page = new byte[pageSize];
                    byte[] records = byteOutputStream.toByteArray();
                    int numberBytesToCopy = byteOutputStream.size();
                    System.arraycopy(records, 0, page, 0, numberBytesToCopy);
                    writeOut(outputStream, page);
                    numberOfPagesUsed++;
                    byteOutputStream.reset();
                }
            }

            // At end of csv, check if there are records in the current page to be written out
            if (numRecordsLoaded % numRecordsPerPage != 0) {
                dataOutput.flush();
                byte[] page = new byte[pageSize];
                byte[] records = byteOutputStream.toByteArray();
                int numberBytesToCopy = byteOutputStream.size();
                System.arraycopy(records, 0, page, 0, numberBytesToCopy);
                writeOut(outputStream, page);
                numberOfPagesUsed++;
                byteOutputStream.reset();
            }

            finishTime = System.nanoTime();
        }
        catch (FileNotFoundException e) {
            System.err.println("Error: File not present " + e.getMessage());
            exceptionOccurred = true;
        }
        catch (IOException e) {
            System.err.println("Error: IOExeption " + e.getMessage());
            exceptionOccurred = true;
        }
        catch (ParseException e) {
            System.err.println("Parse error when parsing date: " + e.getMessage());
        }
        finally {
            // close input/output streams
            if (reader != null) {
                reader.close();
            }
            if (dataOutput != null) {
                dataOutput.close();
            }
            if (byteOutputStream != null) {
                byteOutputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }

        // print out stats if all operations succeeded
        if (exceptionOccurred == false) {

            System.out.println("The number of records loaded: " + numRecordsLoaded);
            System.out.println("The number of pages used: " + numberOfPagesUsed);
            long timeInMilliseconds = (finishTime - startTime)/constants.MILLISECONDS_PER_SECOND;
            System.out.println("Time taken: " + timeInMilliseconds + " ms");
        }
    }

    // Writes out a byte array to file using a FileOutputStream
    public static void writeOut(FileOutputStream stream, byte[] byteArray)
            throws FileNotFoundException, IOException {

        stream.write(byteArray);
    }

    // Returns a whitespace padded string of the same length as parameter int length
    public static String getStringOfLength(String original, int length) {

        int lengthDiff = length - original.length();

        // Check difference in string lengths
        if (lengthDiff == 0) {
            return original;
        }
        else if (lengthDiff > 0) {
            // if original string is too short, pad end with whitespace
            StringBuilder string = new StringBuilder(original);
            for (int i = 0; i < lengthDiff; i++) {
                string.append(" ");
            }
            return string.toString();
        }
        else {
            // if original string is too long, shorten to required length
            return original.substring(0, length);
        }
    }
}
