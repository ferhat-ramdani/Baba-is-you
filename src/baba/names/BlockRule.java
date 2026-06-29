package baba.names;

import java.util.Objects;

/**
 * A record representing a block rule implementing the Rule interface.
 */
public record BlockRule(BlockWord block1, OperatorWord op, BlockWord block2) implements Rule {

    /**
     * Constructs a BlockRule with the specified block words and operator word.
     *
     * @param block1 The first block word.
     * @param op     The operator word.
     * @param block2 The second block word.
     * @throws NullPointerException if any of the parameters is null.
     */
    public BlockRule {
        Objects.requireNonNull(block1);
        Objects.requireNonNull(op);
        Objects.requireNonNull(block2);
    }
}
