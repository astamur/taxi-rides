package dev.astamur.taxirides.tree;

public interface ValueCollectionProvider<V, R> {
    ValueCollection<V, R> emptyCollection();

    ValueCollection<V, R> collectionOf(V value);
}


