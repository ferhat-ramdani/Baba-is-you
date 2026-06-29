package baba.names;

/**
 * An enumeration representing property words.
 */
public enum PropertyWord implements WordName {

    /** Represents the word "YOU". */
    YOU,

    /** Represents the word "WIN". */
    WIN,

    /** Represents the word "STOP". */
    STOP,

    /** Represents the word "PUSH". */
    PUSH,

    /** Represents the word "SINK". */
    SINK,

    /** Represents the word "DEFEAT". */
    DEFEAT,

    /** Represents the word "MELT". */
    MELT,

    /** Represents the word "HOT". */
    HOT,

    /** Represents the word "STICK". */
    STICK;

    /**
     * Checks if the word is a property word.
     *
     * @return true since all words in this enumeration are property words.
     */
    @Override
    public boolean isPropertyWord() {
        return true;
    }
}
