package baba.engine;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.github.forax.zen.Application;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.Event;
import com.github.forax.zen.KeyboardEvent;

public class MenuController {
    private enum State {
        FOLDER_SELECTION,
        LEVEL_SELECTION
    }

    private State currentState = State.FOLDER_SELECTION;
    private List<String> folders = new ArrayList<>();
    private List<String> levels = new ArrayList<>();
    private int selectedIndex = 0;
    
    private String selectedFolder = null;
    private String selectedLevel = null;
    private boolean exit = false;

    private final Path levelsPath;

    public MenuController() {
        var userDir = System.getProperty("user.dir");
        levelsPath = Path.of(userDir, "src", "levels");
        loadFolders();
    }

    private void loadFolders() {
        folders.clear();
        if (Files.exists(levelsPath)) {
            try (Stream<Path> stream = Files.list(levelsPath)) {
                stream.filter(Files::isDirectory)
                      .map(p -> p.getFileName().toString())
                      .filter(name -> !name.equals("default"))
                      .forEach(folders::add);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        selectedIndex = 0;
    }

    private void loadLevels(String folderName) {
        levels.clear();
        Path folderPath = levelsPath.resolve(folderName);
        if (Files.exists(folderPath)) {
            try (Stream<Path> stream = Files.list(folderPath)) {
                stream.filter(p -> p.toString().endsWith(".txt"))
                      .forEach(p -> {
                          String name = p.getFileName().toString();
                          levels.add(name.substring(0, name.length() - 4));
                      });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (levels.isEmpty()) {
            levels.add("1");
        }
        // sort levels numerically if possible
        levels.sort((a, b) -> {
            try {
                return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
            } catch (NumberFormatException e) {
                return a.compareTo(b);
            }
        });
        selectedIndex = 0;
    }

    public String[] show() {
        Application.run(Color.BLACK, context -> {
            try {
                menuLoop(context);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
        if (exit || selectedFolder == null || selectedLevel == null) {
            return null; // Signals user quit
        }
        return new String[]{"--levels", selectedFolder, "--level", selectedLevel};
    }

    private void menuLoop(ApplicationContext context) throws InterruptedException, IOException {
        while (!exit && selectedLevel == null) {
            context.renderFrame(graphics -> drawMenu(graphics, context));
            Event event = context.pollOrWaitEvent(200);
            if (event != null) {
                handleEvent(context, event);
            }
        }
        context.dispose();
    }

    private void drawMenu(Graphics2D graphics, ApplicationContext context) {
        var screenInfo = context.getScreenInfo();
        int width = (int) screenInfo.width();
        int height = (int) screenInfo.height();

        // Clear background
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, 0, width, height);

        // Draw Title
        graphics.setColor(Color.WHITE);
        Font titleFont = new Font("Monospaced", Font.BOLD, 48);
        graphics.setFont(titleFont);
        String title = "BABA IS YOU";
        FontMetrics metrics = graphics.getFontMetrics(titleFont);
        int titleY = height / 4;
        int titleX = (width - metrics.stringWidth(title)) / 2;
        graphics.drawString(title, titleX, titleY);

        // Draw Subtitle
        Font subFont = new Font("Monospaced", Font.PLAIN, 24);
        graphics.setFont(subFont);
        String subtitle = currentState == State.FOLDER_SELECTION ? "Select Level Folder" : "Select Level";
        metrics = graphics.getFontMetrics(subFont);
        int subY = titleY + 60;
        int subX = (width - metrics.stringWidth(subtitle)) / 2;
        graphics.drawString(subtitle, subX, subY);

        // Draw Options
        List<String> options = currentState == State.FOLDER_SELECTION ? folders : levels;
        int rowHeight = 40;
        int totalOptionsHeight = options.size() * rowHeight;
        int startY = Math.max(subY + 80, (height - totalOptionsHeight) / 2 + rowHeight);

        for (int i = 0; i < options.size(); i++) {
            String option = options.get(i);
            if (i == selectedIndex) {
                graphics.setColor(Color.PINK);
                option = "> " + option + " <";
            } else {
                graphics.setColor(Color.LIGHT_GRAY);
            }
            metrics = graphics.getFontMetrics(subFont);
            int optX = (width - metrics.stringWidth(option)) / 2;
            graphics.drawString(option, optX, startY + i * rowHeight);
        }
    }

    private void handleEvent(ApplicationContext context, Event event) {
        if (event instanceof KeyboardEvent keyboardEvent) {
            if (keyboardEvent.action() == KeyboardEvent.Action.KEY_PRESSED) {
                switch (keyboardEvent.key()) {
                    case UP -> {
                        selectedIndex--;
                        List<String> options = currentState == State.FOLDER_SELECTION ? folders : levels;
                        if (selectedIndex < 0) {
                            selectedIndex = options.size() - 1;
                        }
                    }
                    case DOWN -> {
                        selectedIndex++;
                        List<String> options = currentState == State.FOLDER_SELECTION ? folders : levels;
                        if (selectedIndex >= options.size()) {
                            selectedIndex = 0;
                        }
                    }
                    case UNDEFINED -> { // Maps to ENTER key in Zen
                        if (currentState == State.FOLDER_SELECTION) {
                            if (!folders.isEmpty()) {
                                selectedFolder = folders.get(selectedIndex);
                                loadLevels(selectedFolder);
                                currentState = State.LEVEL_SELECTION;
                            }
                        } else if (currentState == State.LEVEL_SELECTION) {
                            if (!levels.isEmpty()) {
                                selectedLevel = levels.get(selectedIndex);
                            }
                        }
                    }
                    case ESCAPE -> {
                        if (currentState == State.LEVEL_SELECTION) {
                            currentState = State.FOLDER_SELECTION;
                            selectedIndex = folders.indexOf(selectedFolder);
                            if (selectedIndex < 0) selectedIndex = 0;
                            selectedFolder = null;
                        } else {
                            exit = true;
                        }
                    }
                    default -> {}
                }
            }
        }
    }
}
