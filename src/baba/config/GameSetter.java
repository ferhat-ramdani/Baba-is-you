package baba.config;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import baba.grid.Grid;
import baba.names.EmptyRule;

/**
 * GameSetter class provides methods for setting up the game environment.
 */
public record GameSetter() {
  private static Config config;
  private static Grid grid;

  /**
   * Sets up the game environment for the first game.
   *
   * @param args the command-line arguments
   * @throws IOException if an I/O error occurs
   */
  public static void FirstGameSetUp(String[] args) throws IOException {
    var screenInfo = Config.screenInfo();
    var userDir = System.getProperty("user.dir");
    var defaultPath = Path.of(userDir, "src" ,"levels", "default", "1.txt");
    var bgColor = Color.getHSBColor(0.65F, 0.4F, 0.10F);
    config = new Config(screenInfo.width(), screenInfo.height(), defaultPath, 0, 0, bgColor, new EmptyRule());
    var commandParser = new CommandParser(args, config);
    if (!commandParser.parse()) {
      System.exit(1);
    }
    config = commandParser.config();
    levelSetUp();
  }

  /**
   * Sets up the game environment for web (headless) mode.
   * Does NOT open any Swing/AWT window — uses fixed pixel dimensions.
   *
   * @param args  the command-line arguments
   * @param width  screen width in pixels
   * @param height screen height in pixels
   * @throws IOException if an I/O error occurs
   */
  public static void WebGameSetUp(String[] args, int width, int height) throws IOException {
    var userDir = System.getProperty("user.dir");
    var defaultPath = Path.of(userDir, "src", "levels", "default", "1.txt");
    var bgColor = Color.getHSBColor(0.65F, 0.4F, 0.10F);
    config = new Config(width, height, defaultPath, 0, 0, bgColor, new EmptyRule());
    var commandParser = new CommandParser(args, config);
    if (!commandParser.parse()) {
      return;
    }
    config = commandParser.config();
    levelSetUp();
  }

  /**
   * Sets up the level based on the current configuration.
   *
   * @throws IOException if an I/O error occurs
   */
  public static void levelSetUp() throws IOException {
    grid = new Grid(config);
    var levelParser = new LevelParser(grid);
    try {
      config = levelParser.parseLevel(config);
    } catch (NoSuchFileException e) {
      System.out.println("Selected level folder or level " + config.level().getFileName() + " does not exist");
    }
    grid.updateConfig(config);
  }

  /**
   * Sets up the next level.
   *
   * @throws IOException if an I/O error occurs
   */
  public static void nextLevelSetUp() throws IOException {
    config = config.updateLevel(nextLevelPath(config.level()));
    levelSetUp();
  }

  private static Path nextLevelPath(Path level) throws IOException {
    try (var stream = Files.list(level.getParent())) {
      var nbLevels = (int) stream.count();
      var curLevel = Integer.parseInt(level.getFileName().toString().split("\\.")[0]);
      var nextLevel = curLevel == nbLevels ? 1 : curLevel + 1;
      return level.getParent().resolve(nextLevel + ".txt");
    }
  }

  /**
   * Returns the game grid.
   *
   * @return the game grid
   */
  public static Grid gameGrid() {
    return grid;
  }

  /**
   * Returns the game configuration.
   *
   * @return the game configuration
   */
  public static Config gameConfig() {
    return config;
  }
}
