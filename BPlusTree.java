
public class BPlusTree {
    public Bucket root;
    public static int MAX_BUCKET_SIZE = 10;

    public BPlusTree() {
        root = new Bucket(null, MAX_BUCKET_SIZE);
        root.isLeaf = true;
    }

    public void append(long birthDate, int pageNum) {
        this.root.insert(new Node(birthDate, pageNum));
    }

    public int verifyLeafOrder() {
        int nodes = 0;
        Bucket targetBucket = this.root;
        while (targetBucket.isLeaf == false) { // Descend to 'leftmost' leaf node
            targetBucket = targetBucket.children.firstElement();
        }
        while (targetBucket != null) { // Traverse over all leaf buckets.
            for (int i = 0; i < targetBucket.nodes.size(); i++) {
                nodes++;
            }
            targetBucket = targetBucket.nextBucket;
        }
        return nodes;
    }

    public Bucket findBucket(long birthDate) {
        Bucket target = root;
        while (target.children.size() != 0) {
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