package baba.view;

import java.awt.Image;
import java.io.IOException;
import java.nio.file.Path;

import javax.swing.ImageIcon;

import baba.names.BlockName;
import baba.names.BlockWord;
import baba.names.OperatorWord;
import baba.names.PropertyWord;
import baba.names.SpriteName;
import baba.names.WordName;

/**
 * A utility class for loading images.
 */
public class ImageLoader {

    private static Image loadAsIcon(Path pathToIcon) throws IOException {
        var userDir = System.getProperty("user.dir");
        var path = Path.of(userDir, "src", "images").resolve(pathToIcon);
        return new ImageIcon(path.toUri().toURL()).getImage();
    }

    /**
     * Loads an image for the given sprite name.
     *
     * @param spriteName The sprite name for which to load the image.
     * @return The loaded image.
     * @throws IOException if an I/O error occurs.
     */
    public static Image load(SpriteName spriteName) throws IOException {
        return switch (spriteName) {
            case BlockName block -> loadAsIcon(Path.of("blocks", block + ".gif"));
            case WordName word -> {
                yield switch (word) {
                    case BlockWord block -> loadAsIcon(Path.of("texts", "blocks", block + ".gif"));
                    case OperatorWord operator -> loadAsIcon(Path.of("texts", "operators", operator + ".gif"));
                    case PropertyWord property -> loadAsIcon(Path.of("texts", "properties", property + ".gif"));
                };
            }
        };
    }
}
