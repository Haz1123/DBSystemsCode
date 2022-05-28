import java.util.Date;
import java.util.Vector;
import java.util.stream.Collectors;

public class Bucket {
    public Bucket parent;
    public Vector<Bucket> children;
    public Vector<Node> nodes;
    public int maxSize;
    public boolean isLeaf;
    public Bucket nextBucket;
    public int bucketOrder;

    public Bucket(Bucket parent, int maxSize) {
        this(parent, maxSize, false);
    }

    public Bucket(Bucket parent, int maxSize, boolean isLeaf) {
        this.parent = parent;
        this.children = new Vector<Bucket>();
        this.nodes = new Vector<Node>();
        this.maxSize = maxSize;
        this.isLeaf = isLeaf;
    }

    public int getTreeSize() {
        int total = 1;
        for (Bucket b : children) {
            total += b.getTreeSize();
        }
        return total;
    }

    public String getStringRep(int dep) {
        String out = "{";
        out += ("\"Nodes\":[\"");
        out += this.nodes.stream().map(s -> (new Date(s.val).toString())).collect(Collectors.joining("\",\""));
        out += "\"],";
        out += "\"Children\":[";
        if (dep > 0) {
            out += this.children.stream().map(c -> (c.getStringRep(dep - 1))).collect(Collectors.joining(","));
        }
        out += "]}";
        return out;
    }

    public boolean verifyChildren() {
        boolean out = true;
        for (Bucket bucket : children) {
            out = out && bucket.parent == this;
        }
        return out;
    }

    public Node insert(Node node) {
        if (this.isLeaf) {
            // This is a leaf bucket
            this.nodes.add(node);
            this.nodes.sort(null);
            if (this.nodes.size() >= this.maxSize - 1) { // Need to split
                Bucket left = new Bucket(null, this.maxSize, true);
                for (int i = 0; i < this.nodes.size() / 2; i++) {
                    left.nodes.add(this.nodes.get(i));
                }
                Bucket right = new Bucket(null, this.maxSize, true);
                for (int i = this.nodes.size() / 2; i < this.nodes.size(); i++) {
                    right.nodes.add(this.nodes.get(i));
                }
                if (this.parent == null) {
                    // First leaf bucket to be split.
                    // Make this bucket into the root
                    left.parent = this;
                    right.parent = this;
                    this.nodes.clear();
                    this.nodes.add(right.nodes.firstElement());
                    this.children.add(left);
                    left.nextBucket = right;
                    this.children.add(right);
                    this.isLeaf = false;
                } else {
                    right.parent = this.parent;
                    right.nextBucket = this.nextBucket;
                    this.nextBucket = right;
                    this.nodes.clear();
                    for (Node x : left.nodes) {
                        this.nodes.add(x);
                    }
                    int selfIndex = this.parent.children.indexOf(this);
                    this.parent.nodes.add(selfIndex, right.nodes.firstElement());
                    this.parent.children.add(selfIndex + 1, right);
                }
            }
        } else {
            boolean found = false;
            Bucket target = this.children.lastElement(); // Default to last bucket
            for (int i = 0; i < this.nodes.size() && !found; i++) {
                if (node.val < this.nodes.get(i).val) {
                    found = true;
                    target = this.children.get(i);
                }
            }
            target.insert(node);
            if (this.nodes.size() >= this.maxSize - 1) {
                // This bucket needs to be split.
                Bucket left = new Bucket(this, maxSize);
                Bucket right = new Bucket(this, maxSize);
                for (int i = 0; i < this.nodes.size() / 2; i++) {
                    left.nodes.add(this.nodes.get(i));
                    left.children.add(this.children.get(i));
                }
                left.children.add(this.children.get((int) this.nodes.size() / 2));
                left.children.forEach(c -> c.parent = left);
                for (int i = this.nodes.size() / 2; i < this.nodes.size(); i++) {
                    right.nodes.add(this.nodes.get(i));
                    right.children.add(this.children.get(i + 1));
                    this.children.get(i + 1).parent = right;
                }
                Node pushUp = right.nodes.firstElement();
                right.nodes.remove(pushUp);
                if (this.parent == null) {
                    // This bucket is the root node.
                    this.nodes.clear();
                    this.children.clear();
                    this.nodes.add(pushUp);
                    this.children.add(left);
                    this.children.add(right);
                } else {
                    // This bucket is on the tree
                    this.nodes.clear();
                    this.children.clear();
                    this.nodes.addAll(left.nodes);
                    this.children.addAll(left.children);
                    this.children.forEach(c -> c.parent = this);
                    int selfIndex = this.parent.children.indexOf(this);
                    right.parent = this.parent;
                    this.parent.nodes.add(selfIndex, pushUp);
                    this.parent.children.add(selfIndex + 1, right);
                }
            }
        }
        if (!this.verifyChildren()) {
            System.out.println(";");
        }
        return null;
    }

}