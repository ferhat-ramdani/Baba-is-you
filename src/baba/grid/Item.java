package baba.grid;

import java.awt.Image;

import baba.names.SpriteName;

/**
 * Item represents an element in the game grid, which can be either a block or a word.
 */
public sealed interface Item permits Block, Word {
    /**
     * Gets the image associated with the item.
     *
     * @return the image of the item
     */
    Image image();

    /**
     * Gets the name of the item.
     *
     * @return the name of the item
     */
    SpriteName name();

    /**
     * Gets the tile where the item is located.
     *
     * @return the tile of the item
     */
    Tile tile();

    /**
     * Moves the item in the specified direction.
     *
     * @param direction the direction to move the item
     * @return the moved item
     */
    Item move(Direction direction);

    /**
     * Checks if the item is a word.
     *
     * @return true if the item is a word, false otherwise
     */
    default boolean isWord() {
        return false;
    }
}
