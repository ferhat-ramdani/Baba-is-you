package baba.names;

import java.util.Objects;

/**
 * A record representing a property rule.
 */
public record PropRule(BlockWord block, OperatorWord op, PropertyWord prop) implements Rule {

    /**
     * Constructs a new property rule.
     *
     * @param block The block word of the rule.
     * @param op    The operator word of the rule.
     * @param prop  The property word of the rule.
     * @throws NullPointerException if any of the parameters are null.
     */
    public PropRule {
        Objects.requireNonNull(block);
        Objects.requireNonNull(op);
        Objects.requireNonNull(prop);
    }
}
