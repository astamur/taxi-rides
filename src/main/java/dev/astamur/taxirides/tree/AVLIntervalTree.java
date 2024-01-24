package dev.astamur.taxirides.tree;


/**
 * An interval tree based on AVL tree and augmented with interval's end.
 *
 * @param <V>
 * @param <R>
 */
public class AVLIntervalTree<V, R> implements IntervalTree<V, R> {
    private final ValueCollectionProvider<V, R> collectionProvider;
    private Node<V, R> root;

    public AVLIntervalTree(ValueCollectionProvider<V, R> collectionProvider) {
        this.collectionProvider = collectionProvider;
    }

    public ValueCollection<V, R> get(Interval interval) {
        return get(root, interval);
    }

    public void insert(Interval interval, V value) {
        root = insert(root, interval, value);
    }

    public ValueCollection<V, R> search(Interval interval) {
        var result = collectionProvider.emptyCollection();
        search(root, interval, result);
        return result;
    }

    public void print() {
        print(root);
    }

    private ValueCollection<V, R> get(Node<V, R> node, Interval interval) {
        if (node == null) {
            return null;
        }
        int cmp = node.compareTo(interval);
        if (cmp > 0) {
            return get(node.left, interval);
        } else if (cmp < 0) {
            return get(node.right, interval);
        } else {
            return node.values;
        }
    }

    private Node<V, R> insert(Node<V, R> node, Interval interval, V value) {
        if (node == null) {
            return new Node<>(interval, collectionProvider.collectionOf(value));
        }
        int cmp = node.compareTo(interval);
        if (cmp > 0) {
            node.left = insert(node.left, interval, value);
        } else if (cmp < 0) {
            node.right = insert(node.right, interval, value);
        } else {
            node.values.append(value);
            return node;
        }

        return rebalance(node, interval);
    }

    private void search(Node<V, R> node, Interval query, ValueCollection<V, R> collection) {
        if (node == null) {
            return;
        }
        if (node.isOverlappedBy(query)) {
            collection.merge(node.values);
        }
        if (node.left != null && node.left.max >= query.start()) {
            search(node.left, query, collection);
        }
        if (node.right != null && node.start < query.end()) {
            search(node.right, query, collection);
        }
    }

    private int height(Node<V, R> node) {
        return (node == null) ? 0 : node.height;
    }

    private int balanceFactor(Node<V, R> node) {
        return (node == null) ? 0 : height(node.left) - height(node.right);
    }

    private void updateNode(Node<V, R> node) {
        if (node == null) {
            return;
        }
        node.height = Math.max(height(node.left), height(node.right)) + 1;
        node.max = maxOf(node.end, max(node.left), max(node.right));
    }

    private Node<V, R> rotateRight(Node<V, R> node) {
        Node<V, R> left = node.left;
        node.left = left.right;
        left.right = node;

        updateNode(node);
        updateNode(left);

        return left;
    }

    private Node<V, R> rotateLeft(Node<V, R> node) {
        Node<V, R> right = node.right;
        node.right = right.left;
        right.left = node;

        updateNode(node);
        updateNode(right);

        return right;
    }

    private Node<V, R> rebalance(Node<V, R> node, Interval newInterval) {
        updateNode(node);
        int balance = balanceFactor(node);

        if (balance > 1) {
            if (node.left.compareTo(newInterval) < 0) {
                node.left = rotateLeft(node.left);
            }
            return rotateRight(node);
        } else if (balance < -1) {
            if (node.right.compareTo(newInterval) > 0) {
                node.right = rotateRight(node.right);
            }
            return rotateLeft(node);
        }

        return node;
    }

    private long max(Node<V, R> node) {
        if (node == null) {
            return Long.MIN_VALUE;
        }
        return node.max;
    }

    private long maxOf(long a, long b, long c) {
        return Math.max(a, Math.max(b, c));
    }

    private void print(Node<V, R> node) {
        if (node == null) {
            return;
        }
        print(node.left);
        System.out.printf("%s:%s\n", node, node.values);
        print(node.right);
    }

    private static class Node<V, R> {
        Node<V, R> left, right;
        ValueCollection<V, R> values;
        int height;
        long start, end, max;

        Node(Interval interval, ValueCollection<V, R> values) {
            this.values = values;
            this.height = 1;
            this.end = interval.end();
            this.start = interval.start();
            this.max = interval.end();
        }

        int compareTo(Interval interval) {
            if (start < interval.start()) {
                return -1;
            } else if (start > interval.start()) {
                return 1;
            } else {
                return Long.compare(end, interval.end());
            }
        }

        boolean isOverlappedBy(Interval interval) {
            return !(start < interval.start() || end > interval.end());
        }

        @Override
        public String toString() {
            return "Node{" +
                "start=" + start +
                "end=" + end +
                ", max=" + max +
                '}';
        }
    }
}
