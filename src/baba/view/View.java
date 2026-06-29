package baba.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Objects;

import baba.config.Config;
import baba.grid.Grid;
import baba.grid.Item;

/**
 * Represents the view of the game.
 */
public record View(Config config, int tileSize, int marginLeft, int marginTop) {

    /**
     * Constructs a view with specified configuration and view parameters.
     *
     * @param config     The game configuration.
     * @param tileSize   The size of each tile.
     * @param marginLeft The left margin.
     * @param marginTop  The top margin.
     * @throws NullPointerException     if the configuration is null.
     * @throws IllegalArgumentException if tileSize, marginLeft, or marginTop is negative.
     */
    public View {
        Objects.requireNonNull(config);
        if (tileSize < 0 || marginLeft < 0 || marginTop < 0) {
            throw new IllegalArgumentException("Negative view value");
        }
        tileSize = Math.min(config.width() / config.columns(), config.height() / config.rows());
        marginLeft = (config.width() - (config.columns() * tileSize)) / 2;
        marginTop = (config.height() - (config.rows() * tileSize)) / 2;
    }

    /**
     * Constructs a view with specified configuration and default view parameters.
     *
     * @param config The game configuration.
     */
    public View(Config config) {
        this(config, 0, 0, 0);
    }

    /**
     * Clears the grid with a specified background color.
     *
     * @param graphics The graphics object.
     */
    public void clearGrid(Graphics2D graphics) {
        graphics.setColor(Color.BLACK);
        graphics.fillRect(marginLeft, marginTop, config.columns() * tileSize, config.rows() * tileSize);
    }

    private void drawItem(Graphics2D graphics, Item item) {
        Objects.requireNonNull(item);
        var width = item.image().getWidth(null);
        var height = item.image().getHeight(null);
        var scale = Math.min((double) tileSize / width, (double) tileSize / height);
        var transform = new AffineTransform(scale, 0, 0, scale, (item.tile().column() - 1) * tileSize + marginLeft,
                (item.tile().row() - 1) * tileSize + marginTop);
        graphics.drawImage(item.image(), transform, null);
    }

    private void drawItemList(Graphics2D graphics, ArrayList<Item> items) {
        Objects.requireNonNull(items);
        for (var item : items) {
            drawItem(graphics, item);
        }
    }

    /**
     * Draws the grid with items on the graphics.
     *
     * @param graphics The graphics object.
     * @param grid     The grid to draw.
     */
    public void drawGrid(Graphics2D graphics, Grid grid) {
        clearGrid(graphics);
        grid.itemsByTile().values().forEach(list -> drawItemList(graphics, list));
    }
}
