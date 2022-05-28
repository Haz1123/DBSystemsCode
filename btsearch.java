import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class btsearch {

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
        int numRecordsPerPage = pageSize / numBytesInOneRecord;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        byte[] page = new byte[pageSize];
        FileInputStream inStream = null;

        // Start by finding the page we want to start at.
        try {
            inStream = new FileInputStream(datafile);
            int numBytesRead = 0;
            startTime = System.nanoTime();
        } catch (FileNotFoundException e) {
            System.err.println("File not found " + e.getMessage());
        } finally {
            if (inStream != null) {
                inStream.close();
            }
        }
    }
}
