package baba.grid;

import java.awt.Image;
import java.io.IOException;
import java.util.Objects;

import baba.names.BlockName;
import baba.view.ImageLoader;

/**
 * Block represents a block item in the grid.
 */
public record Block(Image image, BlockName name, Tile tile) implements Item {

    /**
     * Constructs a Block with the specified image, name, and tile.
     *
     * @param image the image representing the block
     * @param name  the name of the block
     * @param tile  the tile where the block is located
     * @throws NullPointerException if any argument is null
     */
    public Block {
        Objects.requireNonNull(image);
        Objects.requireNonNull(name);
        Objects.requireNonNull(tile);
    }

    /**
     * Updates the tile of the block.
     *
     * @param tile the new tile
     * @return a new Block with the updated tile
     */
    private Block updateTile(Tile tile) {
        return new Block(image, name, tile);
    }

    /**
     * Changes the sprite of the block.
     *
     * @param name the new name of the block
     * @return a new Block with the updated sprite
     * @throws IOException if the image loading fails
     */
    public Block changeSprite(BlockName name) throws IOException {
        return new Block(ImageLoader.load(name), name, tile);
    }

    /**
     * Moves the block in the specified direction.
     *
     * @param direction the direction to move
     * @return a new Block with the updated tile after movement
     */
    @Override
    public Item move(Direction direction) {
        return updateTile(tile.adjacentTile(direction));
    }

    /**
     * Returns a string representation of the block.
     *
     * @return a string describing the block
     */
    @Override
    public String toString() {
        return name + " in " + tile;
    }
}
