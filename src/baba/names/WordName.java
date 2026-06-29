package baba.names;

/**
 * A sealed interface representing word names.
 */
public sealed interface WordName extends SpriteName permits BlockWord, OperatorWord, PropertyWord {

    /**
     * Checks if the word name is a word.
     *
     * @return always true by default.
     */
    @Override
    default boolean isWord() {
        return true;
    }

    /**
     * Checks if the word name is a block word.
     *
     * @return always false by default.
     */
    default boolean isBlockWord() {
        return false;
    }

    /**
     * Checks if the word name is an operator word.
     *
     * @return always false by default.
     */
    default boolean isOperatorWord() {
        return false;
    }

    /**
     * Checks if the word name is a property word.
     *
     * @return always false by default.
     */
    default boolean isPropertyWord() {
        return false;
    }
}
