package baba.names;

/**
 * A sealed interface representing rules.
 */
public sealed interface Rule permits BlockRule, PropRule, EmptyRule {

}
