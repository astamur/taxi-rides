package dev.astamur.taxirides.tree;


import dev.astamur.taxirides.model.Interval;
import dev.astamur.taxirides.processor.Collector;
import dev.astamur.taxirides.processor.CollectorProvider;
import java.time.Instant;

/**
 * An interval tree based on AVL tree and augmented with interval's end.
 * Intervals are considered as closed (boundaries are included). Duplicates are allowed.
 *
 * @param <V> a type of incoming value
 * @param <R> a result type of the used value collector
 * @param <C> a collector for values in each node
 */
public class AVLIntervalTree<V, R, C extends Collector<V, R, C>> implements IntervalTree<V, R, C> {
    private final CollectorProvider<V, R, C> collectionProvider;
    private Node<V, R, C> root;

    public AVLIntervalTree(CollectorProvider<V, R, C> collectionProvider) {
        this.collectionProvider = collectionProvider;
    }

    public C get(Interval interval) {
        return get(root, interval);
    }

    public void insert(Interval interval, V value) {
        root = insert(root, interval, value);
    }

    public C search(Interval interval) {
        var result = collectionProvider.create();
        search(root, interval, result);
        return result;
    }

    public void print() {
        print(root);
    }

    private C get(Node<V, R, C> node, Interval interval) {
        if (node == null) {
            return null;
        }
        int cmp = node.compareTo(interval);
        if (cmp > 0) {
            return get(node.left, interval);
        } else if (cmp < 0) {
            return get(node.right, interval);
        } else {
            return node.collector;
        }
    }

    private Node<V, R, C> insert(Node<V, R, C> node, Interval interval, V value) {
        if (node == null) {
            return new Node<>(interval, collectionProvider.create(value));
        }
        int cmp = node.compareTo(interval);
        if (cmp > 0) {
            node.left = insert(node.left, interval, value);
        } else if (cmp < 0) {
            node.right = insert(node.right, interval, value);
        } else {
            node.collector.add(value);
            return node;
        }

        return rebalance(node, interval);
    }

    private void search(Node<V, R, C> node, Interval query, C result) {
        if (node == null) {
            return;
        }
        if (node.isOverlappedBy(query)) {
            result.merge(node.collector);
        }
        if (node.left != null && node.left.max >= query.start()) {
            search(node.left, query, result);
        }
        if (node.right != null && node.start < query.end()) {
            search(node.right, query, result);
        }
    }

    private int height(Node<V, R, C> node) {
        return (node == null) ? 0 : node.height;
    }

    private int balanceFactor(Node<V, R, C> node) {
        return (node == null) ? 0 : height(node.left) - height(node.right);
    }

    private void updateNode(Node<V, R, C> node) {
        if (node == null) {
            return;
        }
        node.height = Math.max(height(node.left), height(node.right)) + 1;
        node.max = maxOf(node.end, max(node.left), max(node.right));
    }

    private Node<V, R, C> rotateRight(Node<V, R, C> node) {
        Node<V, R, C> left = node.left;
        node.left = left.right;
        left.right = node;

        updateNode(node);
        updateNode(left);

        return left;
    }

    private Node<V, R, C> rotateLeft(Node<V, R, C> node) {
        Node<V, R, C> right = node.right;
        node.right = right.left;
        right.left = node;

        updateNode(node);
        updateNode(right);

        return right;
    }

    private Node<V, R, C> rebalance(Node<V, R, C> node, Interval newInterval) {
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

    private long max(Node<V, R, C> node) {
        if (node == null) {
            return Long.MIN_VALUE;
        }
        return node.max;
    }

    private long maxOf(long a, long b, long c) {
        return Math.max(a, Math.max(b, c));
    }

    private void print(Node<V, R, C> node) {
        if (node == null) {
            return;
        }
        print(node.left);
        System.out.printf("%s\n", node);
//        System.out.printf("%s:%s\n", node, node.collector);
        print(node.right);
    }

    private static class Node<V, R, C> {
        Node<V, R, C> left, right;
        C collector;
        int height;
        long start, end, max;

        Node(Interval interval, C collector) {
            this.collector = collector;
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
                "start=" + Instant.ofEpochMilli(start) +
                ", end=" + Instant.ofEpochMilli(end) +
                ", max=" + Instant.ofEpochMilli(max) +
                '}';
        }
    }
}
