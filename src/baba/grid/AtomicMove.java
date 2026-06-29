package baba.grid;

import java.util.Objects;

/**
 * AtomicMove represents an atomic movement of a single item.
 */
public record AtomicMove(Item prevItem, Item newItem) {
    /**
     * Constructs an AtomicMove with the specified previous and new states.
     *
     * @param prevItem the previous item
     * @param newItem  the new item
     * @throws NullPointerException if prevItem or newItem is null
     */
    public AtomicMove {
        Objects.requireNonNull(prevItem);
        Objects.requireNonNull(newItem);
    }

    /**
     * Returns a string representation of the atomic move.
     *
     * @return a string describing the move
     */
    @Override
    public String toString() {
        return "from " + prevItem + " to " + newItem;
    }
}
