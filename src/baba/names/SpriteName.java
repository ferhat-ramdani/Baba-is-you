package baba.names;

/**
 * A sealed interface representing sprite names.
 */
public sealed interface SpriteName permits BlockName, WordName {

    /**
     * Checks if the sprite name is a word.
     *
     * @return always false by default.
     */
    default boolean isWord() {
        return false;
    }
}
