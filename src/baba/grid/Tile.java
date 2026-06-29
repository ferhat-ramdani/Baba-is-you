package baba.grid;

import baba.config.Config;

/**
 * Tile represents a position in the game grid.
 */
public record Tile(int row, int column) {
    /**
     * Constructs a Tile with the specified row and column.
     *
     * @param row    the row of the tile
     * @param column the column of the tile
     * @throws IllegalArgumentException if the row or column is less than 1
     */
    public Tile {
        if (row < 1 || column < 1) {
            throw new IllegalArgumentException("Tile outside of bounds");
        }
    }

    /**
     * Returns the tile adjacent to this tile in the specified direction.
     *
     * @param direction the direction of the adjacent tile
     * @return the adjacent tile
     */
    public Tile adjacentTile(Direction direction) {
        return switch (direction) {
            case LEFT -> new Tile(row, column - 1);
            case UP -> new Tile(row - 1, column);
            case RIGHT -> new Tile(row, column + 1);
            case DOWN -> new Tile(row + 1, column);
        };
    }

    /**
     * Checks if this tile touches the border of the grid in the specified direction.
     *
     * @param direction the direction to check
     * @param config    the game configuration
     * @return true if the tile touches the border in the specified direction, false otherwise
     */
    public boolean touchesBorder(Direction direction, Config config) {
        return switch (direction) {
            case LEFT -> column == 1;
            case UP -> row == 1;
            case RIGHT -> column == config.columns();
            case DOWN -> row == config.rows();
        };
    }

    @Override
    public String toString() {
        return "(" + row + ", " + column + ")";
    }
}
