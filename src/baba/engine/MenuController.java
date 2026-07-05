package baba.engine;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.github.forax.zen.Application;
import com.github.forax.zen.ApplicationContext;
import com.github.forax.zen.KeyboardEvent;

public class MenuController {
    private enum State {
        FOLDER_SELECTION,
        LEVEL_SELECTION
    }

    private State currentState = State.FOLDER_SELECTION;
    private final List<String> folders = new ArrayList<>();
    private final List<String> levels = new ArrayList<>();
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
            try (var stream = Files.list(levelsPath)) {
                stream.filter(Files::isDirectory)
                      .map(p -> p.getFileName().toString())
                      .filter(name -> !name.equals("default"))
                      .forEach(folders::add);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        folders.sort((a, b) -> {
            if (a.equals("original")) return -1;
            if (b.equals("original")) return 1;
            if (a.equals("exclusive")) return -1;
            if (b.equals("exclusive")) return 1;
            return a.compareTo(b);
        });
        selectedIndex = 0;
    }

    private void loadLevels(String folderName) {
        levels.clear();
        var folderPath = levelsPath.resolve(folderName);
        if (Files.exists(folderPath)) {
            try (var stream = Files.list(folderPath)) {
                stream.filter(p -> p.toString().endsWith(".txt"))
                      .forEach(p -> {
                          var name = p.getFileName().toString();
                          levels.add(name.substring(0, name.length() - 4));
                      });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (levels.isEmpty()) {
            levels.add("1");
        }
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
            return null;
        }
        return new String[]{"--levels", selectedFolder, "--level", selectedLevel};
    }

    private void menuLoop(ApplicationContext context) throws InterruptedException, IOException {
        var lastRenderTime = 0L;
        var firstFrame = true;
        while (!exit && selectedLevel == null) {
            var now = System.currentTimeMillis();
            var waitTime = Math.max(0, 200 - (now - lastRenderTime));
            var event = context.pollOrWaitEvent(firstFrame ? 0 : waitTime);
            
            var handled = false;
            if (event instanceof KeyboardEvent keyboardEvent) {
                if (keyboardEvent.action() == KeyboardEvent.Action.KEY_PRESSED) {
                    handled = true;
                    switch (keyboardEvent.key()) {
                        case UP -> {
                            selectedIndex--;
                            var options = currentState == State.FOLDER_SELECTION ? folders : levels;
                            if (selectedIndex < 0) {
                                selectedIndex = options.size() - 1;
                            }
                        }
                        case DOWN -> {
                            selectedIndex++;
                            var options = currentState == State.FOLDER_SELECTION ? folders : levels;
                            if (selectedIndex >= options.size()) {
                                selectedIndex = 0;
                            }
                        }
                        case UNDEFINED -> {
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
            
            now = System.currentTimeMillis();
            if (firstFrame || handled || now - lastRenderTime >= 200) {
                firstFrame = false;
                lastRenderTime = now;
                context.renderFrame(graphics -> {
                    var screenInfo = context.getScreenInfo();
                    var width = (int) screenInfo.width();
                    var height = (int) screenInfo.height();

                    graphics.setColor(Color.BLACK);
                    graphics.fillRect(0, 0, width, height);

                    graphics.setColor(Color.WHITE);
                    var titleFont = new Font("Monospaced", Font.BOLD, 72);
                    graphics.setFont(titleFont);
                    var title = "BABA IS YOU";
                    var metrics = graphics.getFontMetrics(titleFont);
                    var titleY = height / 4;
                    var titleX = (width - metrics.stringWidth(title)) / 2;
                    graphics.drawString(title, titleX, titleY);

                    var subFont = new Font("Monospaced", Font.PLAIN, 36);
                    graphics.setFont(subFont);
                    var subtitle = currentState == State.FOLDER_SELECTION ? "Select Level Folder" : "Select Level";
                    metrics = graphics.getFontMetrics(subFont);
                    var subY = titleY + 80;
                    var subX = (width - metrics.stringWidth(subtitle)) / 2;
                    graphics.drawString(subtitle, subX, subY);

                    var options = currentState == State.FOLDER_SELECTION ? folders : levels;
                    var rowHeight = 60;
                    var totalOptionsHeight = options.size() * rowHeight;
                    var startY = Math.max(subY + 120, (height - totalOptionsHeight) / 2 + rowHeight);

                    for (var i = 0; i < options.size(); i++) {
                        var option = options.get(i);
                        if (i == selectedIndex) {
                            graphics.setColor(Color.PINK);
                            option = "> " + option + " <";
                        } else {
                            graphics.setColor(Color.LIGHT_GRAY);
                        }
                        metrics = graphics.getFontMetrics(subFont);
                        var optX = (width - metrics.stringWidth(option)) / 2;
                        graphics.drawString(option, optX, startY + i * rowHeight);
                    }

                    if (currentState == State.FOLDER_SELECTION && !folders.isEmpty()) {
                        var selected = folders.get(selectedIndex);
                        var desc = "";
                        if (selected.equals("original")) {
                            desc = "Experience the classic journey: 6 iconic levels from the original puzzle.";
                        } else if (selected.equals("exclusive")) {
                            desc = "Venture into the unknown: entirely new puzzles featuring unseen mechanics.";
                        }
                        if (!desc.isEmpty()) {
                            var descFont = new Font("Monospaced", Font.ITALIC, 24);
                            graphics.setFont(descFont);
                            graphics.setColor(Color.LIGHT_GRAY);
                            var descMetrics = graphics.getFontMetrics(descFont);
                            var descX = (width - descMetrics.stringWidth(desc)) / 2;
                            var descY = startY + totalOptionsHeight + 80;
                            graphics.drawString(desc, descX, descY);
                        }
                    }
                });
            }
        }
        context.dispose();
    }
}
