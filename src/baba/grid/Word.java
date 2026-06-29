package baba.grid;

import java.awt.Image;
import java.util.Objects;

import baba.names.WordName;

/**
 * Word represents a word entity in the game grid.
 */
public record Word(Image image, WordName name, Tile tile) implements Item {
    /**
     * Constructs a Word with the specified image, name, and tile.
     *
     * @param image the image of the word
     * @param name  the name of the word
     * @param tile  the tile where the word is located
     * @throws NullPointerException if image, name, or tile is null
     */
    public Word {
        Objects.requireNonNull(image);
        Objects.requireNonNull(name);
        Objects.requireNonNull(tile);
    }

    private Word updateTile(Tile tile) {
        return new Word(image, name, tile);
    }

    /**
     * Moves the word in the specified direction.
     *
     * @param direction the direction in which to move the word
     * @return the updated Item object after the movement
     */
    @Override
    public Item move(Direction direction) {
        return updateTile(tile.adjacentTile(direction));
    }

    /**
     * Returns a string representation of the Word object.
     *
     * @return a string representation of the Word
     */
    @Override
    public String toString() {
        return name + " in " + tile;
    }

    /**
     * Checks if the item is a word.
     *
     * @return true if the item is a word, false otherwise
     */
    @Override
    public boolean isWord() {
        return true;
    }
}
