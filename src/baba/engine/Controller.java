package baba.engine;

import java.io.IOException;

import com.github.forax.zen.Application;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.Event;
import com.github.forax.zen.KeyboardEvent;
import com.github.forax.zen.PointerEvent;

import baba.config.Config;
import baba.grid.Direction;
import baba.grid.Grid;
import baba.names.PropertyWord;
import baba.view.View;

/**
 * Controller class manages the game logic and user input handling.
 */
public class Controller {
    private final Config config;
    private final Grid grid;
    private boolean playing = true;
    private boolean win = false;

    /**
     * Constructs a Controller with the specified grid and config.
     *
     * @param grid   the game grid
     * @param config the game configuration
     */
    public Controller(Grid grid, Config config) {
        this.grid = grid;
        this.config = config;
    }

    /**
     * Starts the game loop.
     */
    public void start() {
        Application.run(config.bgColor(), context -> {
            View view = new View(config);
            try {
                gameLoop(context, view, grid);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Checks if the game is won.
     *
     * @return true if the game is won, false otherwise
     */
    public boolean gameWin() {
        return win;
    }

    private void gameLoop(ApplicationContext context, View view, Grid grid) throws InterruptedException, IOException {
        while (playing && !win) {
            grid.searchRule();
            grid.saveMove();
            context.renderFrame(graphics -> {
                view.drawGrid(graphics, grid);
            });
            var event = context.pollOrWaitEvent(200);
            if (event != null && handleEvent(context, event) && playing) {
                grid.checkRule(PropertyWord.SINK);
                grid.checkRule(PropertyWord.YOU, PropertyWord.DEFEAT);
                grid.checkRule(PropertyWord.MELT, PropertyWord.HOT);
                grid.checkRule(PropertyWord.STICK);
                if (grid.checkRule(PropertyWord.YOU, PropertyWord.WIN)) {
                    win = true;
                }
            }
        }
        context.dispose();
    }

    private boolean handleEvent(ApplicationContext context, Event event) {
        switch (event) {
            case KeyboardEvent keyboardEvent -> {
                if (keyboardEvent.action() == KeyboardEvent.Action.KEY_PRESSED) {
                    return playKeyboard(keyboardEvent, context);
                }
            }
            case PointerEvent pointerEvent -> {
            }
        }
        return false;
    }

    private boolean playKeyboard(KeyboardEvent keyboardEvent, ApplicationContext context) {
        switch (keyboardEvent.key()) {
            case UP -> grid.moveYou(Direction.UP);
            case DOWN -> grid.moveYou(Direction.DOWN);
            case LEFT -> grid.moveYou(Direction.LEFT);
            case RIGHT -> grid.moveYou(Direction.RIGHT);
            case ESCAPE, Q -> playing = false;
            case SPACE -> grid.undoMove();
            default -> {
                return false;
            }
        }
        return true;
    }
}
