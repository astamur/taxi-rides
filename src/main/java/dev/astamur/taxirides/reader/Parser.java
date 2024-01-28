package dev.astamur.taxirides.reader;

/**
 * Formats an incoming object of the source type into an object of teh result type.
 *
 * @param <S> a source type
 * @param <R> a result type
 */
public interface Parser<S, R> {
    R parse(S record);
}
