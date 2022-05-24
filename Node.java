
public class Node implements Comparable<Node> {
    public long val;
    public int page;

    public Node(long val, int page) {
        this.val = val;
        this.page = page;
    }

    @Override
    public int compareTo(Node o) {
        if (this.val == o.val) {
            return 0;
        } else if (this.val < o.val) {
            return -1;
        } else {
            return 1;
        }
    }

}