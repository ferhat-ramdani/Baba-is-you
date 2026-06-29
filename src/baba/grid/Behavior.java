package baba.grid;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import baba.names.PropertyWord;

/**
 * Behavior represents the behavior of an item in the grid, defined by its properties.
 */
public class Behavior {
    private final Set<PropertyWord> properties;

    /**
     * Constructs a Behavior object with no initial properties.
     */
    public Behavior() {
        properties = new HashSet<>();
    }

    /**
     * Constructs a Behavior object with the specified properties.
     *
     * @param properties the properties to initialize the behavior with
     */
    public Behavior(PropertyWord... properties) {
        this.properties = new HashSet<>(Set.of(properties));
    }

    /**
     * Adds a property to the behavior.
     *
     * @param property the property to add
     */
    public void addProperty(PropertyWord property) {
        properties.add(property);
    }

    /**
     * Adds multiple properties to the behavior.
     *
     * @param properties the properties to add
     */
    public void addProperties(PropertyWord... properties) {
        this.properties.addAll(Set.of(properties));
    }

    /**
     * Removes a property from the behavior.
     *
     * @param property the property to remove
     */
    public void removeProperty(PropertyWord property) {
        properties.remove(property);
    }

    /**
     * Returns an unmodifiable set of properties associated with the behavior.
     *
     * @return the set of properties
     */
    private Set<PropertyWord> getProperties() {
        return Set.copyOf(properties);
    }

    /**
     * Checks if the behavior has a specific property.
     *
     * @param property the property to check
     * @return true if the behavior has the property, false otherwise
     */
    public boolean hasProperty(PropertyWord property) {
        return property == null || properties.contains(property);
    }

    /**
     * Returns a string representation of the behavior.
     *
     * @return a string describing the behavior
     */
    @Override
    public String toString() {
        if (properties.isEmpty()) {
            return "NOTHING";
        }
        StringBuilder builder = new StringBuilder();
        String separator = "";
        for (var property : properties) {
            builder.append(separator).append(property);
            separator = ",";
        }
        return builder.toString();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param o the reference object with which to compare
     * @return true if this behavior is the same as the obj argument; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof Behavior behavior && properties.equals(behavior.getProperties());
    }

    /**
     * Returns a hash code value for the behavior.
     *
     * @return a hash code value for this behavior
     */
    @Override
    public int hashCode() {
        return Objects.hash(properties);
    }
}
