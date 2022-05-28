import java.nio.ByteBuffer;

public class IndexRecord {
    // Index Record: DATA<Long>[9] PAGE<int>[10] OFFSET<int>[10]
    // Bytes: 9*8 10*4 10*4 = 152 bytes.
    public static int RECORD_SIZE = 152;
    private static int DATA_OFFSET = 0;
    private static int PAGE_OFFSET = 72;
    private static int OFFSET_OFFSET = 112;
    public long[] data;
    public int[] page;
    public int[] offset;

    public IndexRecord(byte[] bytes) {
        data = new long[9];
        page = new int[10];
        offset = new int[10];
        for (int i = 0; i < data.length; i++) {
            byte[] longBytes = new byte[Long.BYTES];
            System.arraycopy(bytes, (i * Long.BYTES) + DATA_OFFSET, longBytes, 0, Long.BYTES);
            data[i] = ByteBuffer.wrap(longBytes).getLong();
        }
        for (int i = 0; i < page.length; i++) {
            byte[] pageBytes = new byte[Integer.BYTES];
            System.arraycopy(bytes, (i * Integer.BYTES) + PAGE_OFFSET, pageBytes, 0, Integer.BYTES);
            page[i] = ByteBuffer.wrap(pageBytes).getInt();
        }
        for (int i = 0; i < offset.length; i++) {
            byte[] offsetBytes = new byte[Integer.BYTES];
            System.arraycopy(bytes, (i * Integer.BYTES) + OFFSET_OFFSET, offsetBytes, 0, Integer.BYTES);
            offset[i] = ByteBuffer.wrap(offsetBytes).getInt();
        }
    }

    public boolean isLeaf() {
        return (this.offset[0] == -1 && this.offset[this.offset.length - 1] != -1);
    }
}