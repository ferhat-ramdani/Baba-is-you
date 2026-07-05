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

public class Controller {
    private final Config config;
    private final Grid grid;
    private boolean playing = true;
    private boolean win = false;
    private boolean returnToMenu = false;

    public Controller(Grid grid, Config config) {
        this.grid = grid;
        this.config = config;
    }

    public void start() {
        Application.run(config.bgColor(), context -> {
            var view = new View(config);
            try {
                gameLoop(context, view, grid);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
    }

    public boolean gameWin() {
        return win;
    }

    public boolean isReturnToMenu() {
        return returnToMenu;
    }

    private void gameLoop(ApplicationContext context, View view, Grid grid) throws InterruptedException, IOException {
        long lastRenderTime = 0;
        boolean firstFrame = true;
        while (playing && !win) {
            long now = System.currentTimeMillis();
            long waitTime = Math.max(0, 200 - (now - lastRenderTime));
            var event = context.pollOrWaitEvent(firstFrame ? 0 : waitTime);
            
            boolean handled = false;
            if (event != null) {
                handled = handleEvent(context, event);
                if (handled && playing) {
                    grid.checkRule(PropertyWord.SINK);
                    grid.checkRule(PropertyWord.YOU, PropertyWord.DEFEAT);
                    grid.checkRule(PropertyWord.MELT, PropertyWord.HOT);
                    grid.checkRule(PropertyWord.STICK);
                    if (grid.checkRule(PropertyWord.YOU, PropertyWord.WIN)) {
                        win = true;
                    }
                }
            }
            
            now = System.currentTimeMillis();
            if (firstFrame || handled || now - lastRenderTime >= 200) {
                firstFrame = false;
                lastRenderTime = now;
                grid.searchRule();
                grid.saveMove();
                context.renderFrame(graphics -> {
                    view.drawGrid(graphics, grid);
                });
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
            case ESCAPE -> playing = false;
            case M -> {
                playing = false;
                returnToMenu = true;
            }
            case SPACE -> grid.undoMove();
            default -> {
                return false;
            }
        }
        return true;
    }
}
