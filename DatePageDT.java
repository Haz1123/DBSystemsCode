import java.sql.Date;

public class DatePageDT implements Comparable<DatePageDT> {
    public long val;
    public int page;

    public DatePageDT(long val, int page) {
        this.val = val;
        this.page = page;
    }

    @Override
    public int compareTo(DatePageDT o) {
        if (new Date(this.val).equals(new Date(o.val))) {
            return 0;
        } else if (new Date(this.val).before(new Date(o.val))) {
            return -1;
        } else {
            return 1;
        }
    }

}