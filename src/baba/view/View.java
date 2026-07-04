package baba.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Objects;

import baba.config.Config;
import baba.grid.Grid;
import baba.grid.Item;

public record View(Config config, int tileSize, int marginLeft, int marginTop) {

    public View {
        Objects.requireNonNull(config);
        if (tileSize < 0 || marginLeft < 0 || marginTop < 0) {
            throw new IllegalArgumentException("Negative view value");
        }
        var helpMargin = 60;
        var availableHeight = config.height() - helpMargin;
        tileSize = Math.min(config.width() / config.columns(), availableHeight / config.rows());
        marginLeft = (config.width() - (config.columns() * tileSize)) / 2;
        marginTop = (availableHeight - (config.rows() * tileSize)) / 2;
    }

    public View(Config config) {
        this(config, 0, 0, 0);
    }

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

    private void drawHelpMenu(Graphics2D graphics) {
        var helpText = "ARROWS: Move | SPACE: Undo | M: Menu | ESC: Quit";
        graphics.setColor(Color.LIGHT_GRAY);
        var font = new Font("Monospaced", Font.BOLD, 18);
        graphics.setFont(font);
        var metrics = graphics.getFontMetrics(font);
        var x = (config.width() - metrics.stringWidth(helpText)) / 2;
        var y = config.height() - 20;
        graphics.drawString(helpText, x, y);
    }

    public void drawGrid(Graphics2D graphics, Grid grid) {
        clearGrid(graphics);
        grid.itemsByTile().values().forEach(list -> drawItemList(graphics, list));
        drawHelpMenu(graphics);
    }
}
