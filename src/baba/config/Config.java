package baba.config;

import java.awt.Color;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;

import com.github.forax.zen.Application;
import com.github.forax.zen.ScreenInfo;

import baba.names.Rule;

/**
 * Config class holds the configuration for the Baba Is You game.
 */
public record Config(int width, int height, Path level, int rows, int columns, Color bgColor, Rule specialRule) {

  /**
   * Constructs a new Config object with the given parameters.
   *
   * @param width        the width of the game screen
   * @param height       the height of the game screen
   * @param level        the level to be loaded
   * @param rows         the number of rows in the game
   * @param columns      the number of columns in the game
   * @param bgColor      the background color of the game screen
   * @param specialRule  the special rule to be applied in the game
   * @throws NullPointerException if level is null
   */
  public Config {
    Objects.requireNonNull(level);
    specialRule = Objects.requireNonNull(specialRule);
  }

  /**
   * Returns a copy of the Config object.
   *
   * @return a new Config object with the same values
   */
  public Config copy() {
    return new Config(width, height, level, rows, columns, bgColor, specialRule);
  }

  /**
   * Returns a new Config object with updated number of rows.
   *
   * @param newRows the new number of rows
   * @return a new Config object with the updated number of rows
   */
  public Config updateRows(int newRows) {
    return new Config(width, height, level, newRows, columns, bgColor, specialRule);
  }

  /**
   * Returns a new Config object with updated number of columns.
   *
   * @param newColumns the new number of columns
   * @return a new Config object with the updated number of columns
   */
  public Config updateColumns(int newColumns) {
    return new Config(width, height, level, rows, newColumns, bgColor, specialRule);
  }

  /**
   * Returns a new Config object with updated level.
   *
   * @param newLevel the new level to be loaded
   * @return a new Config object with the updated level
   */
  public Config updateLevel(Path newLevel) {
    return new Config(width, height, newLevel, rows, columns, bgColor, specialRule);
  }

  /**
   * Returns a new Config object with updated special rule.
   *
   * @param newSpecialRule the new special rule to be applied
   * @return a new Config object with the updated special rule
   */
  public Config updateSpecialRule(Rule newSpecialRule) {
    return new Config(width, height, level, rows, columns, bgColor, newSpecialRule);
  }

  /**
   * Returns the ScreenInfo object for the game screen.
   *
   * @return the ScreenInfo object for the game screen
   */
  public static ScreenInfo screenInfo() {
    var info = new ArrayList<ScreenInfo>();
    Application.run(Color.black, context -> {
      info.add(context.getScreenInfo());
      context.dispose();
    });
    return info.get(0);
  }
}
