package baba.names;

/**
 * An enumeration representing operator words for word names.
 */
public enum OperatorWord implements WordName {

    IS;

    /**
     * Checks if the word is an operator word.
     *
     * @return true if the word is an operator word, false otherwise.
     */
    @Override
    public boolean isOperatorWord() {
        return true;
    }
}
