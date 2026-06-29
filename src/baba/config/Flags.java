package baba.config;

/**
 * Flags is an enumeration of the possible command-line flags that can be used in a command obtained from the command-line.
 */
public enum Flags {
    /**
     * The --level flag is used to specify the current level of the Config object.
     */
    level,
    /**
     * The --levels flag is used to specify the folder of levels that are being played.
     */
    levels,
    /**
     * The --execute flag is used to execute a rule given in command line.
     */
    execute
}