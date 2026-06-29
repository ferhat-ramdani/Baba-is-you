package baba.config;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

import baba.grid.Grid;
import baba.grid.Tile;
import baba.names.BlockName;
import baba.names.BlockWord;
import baba.names.OperatorWord;
import baba.names.PropertyWord;
import baba.names.SpriteName;

/**
 * LevelParser class parses the level configuration file and updates the grid accordingly.
 */
public record LevelParser(Grid grid) {

  /**
   * Parses the level configuration file and updates the game configuration.
   *
   * @param config the current game configuration
   * @return the updated game configuration after parsing the level
   * @throws IOException if an I/O error occurs
   */
  public Config parseLevel(Config config) throws IOException {
    Objects.requireNonNull(config);
    Config newConf = config.copy();
    try (var reader = Files.newBufferedReader(config.level())) {
      Modes mode = null;
      String line;
      while ((line = reader.readLine()) != null) {
        if(line.length() == 0) {
          continue;
        }
        try {
          mode = Modes.valueOf(line.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
          var trimmed = line.trim();
          switch (mode) {
            case ROWS -> newConf = newConf.updateRows(Integer.parseInt(trimmed));
            case COLUMNS -> newConf = newConf.updateColumns(Integer.parseInt(trimmed));
            case WORDS -> parseWordLine(trimmed);
            case BLOCKS -> parseBlockLine(trimmed);
          }
        }
      }
    }
    return newConf;
  }

  private List<Integer> coorsFromString(String string) {
    List<Integer> coors = new ArrayList<Integer>();
    var parts = string.split("\\s+");
    for (String coor : parts) {
      coors.add(Integer.parseInt(coor));
    }
    return List.copyOf(coors);
  }

  private List<Tile> tilesFromCoors(List<Integer> coors) {
    var tiles = new ArrayList<Tile>();
    for (var i = 1; i < coors.size(); i = i + 2) {
      tiles.add(new Tile(coors.get(i - 1), coors.get(i)));
    }
    return List.copyOf(tiles);
  }

  static SpriteName nameFromText(String text) {
    var blockNames = Arrays.asList(BlockName.values());
    var blockWords = Arrays.asList(BlockWord.values());
    var operators = Arrays.asList(OperatorWord.values());
    var properties = Arrays.asList(PropertyWord.values());

    var result = Stream.of(blockNames, blockWords, operators, properties).flatMap(Collection::stream)
            .filter(e -> e.toString().equals(text.toUpperCase(Locale.ROOT))).findFirst();

    if (result.isPresent()) {
      return result.get();
    } else {
      throw new IllegalArgumentException("Text " + text + " is not resolved to a SpriteName");
    }
  }

  private void parseSeperatedPart(SpriteName name, String part) throws IOException {
    var coors = coorsFromString(part);
    var tiles = tilesFromCoors(coors);
    Factory.put(grid, name, tiles);
  }

  private void parseLinkedPart(SpriteName name, String part) throws IOException {
    var coors = coorsFromString(part);
    var tiles = tilesFromCoors(coors);
    Factory.putAndLink(grid, name, tiles);
  }

  private void parseWordLine(String line) throws IOException {
    var parts = line.split(":");
    var name = nameFromText(parts[0].trim());
    parseSeperatedPart(name, parts[1].trim());
  }

  private void parseBlockLine(String line) throws IOException {
    var largeParts = line.split(":");
    var name = nameFromText(largeParts[0].trim());
    var layouts = largeParts[1].trim().split(",");
    for (var layout : layouts) {
      var trimmed = layout.trim();
      var part = trimmed.substring(1, trimmed.length() - 1);
      if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
        parseLinkedPart(name, part);
      } else if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
        parseSeperatedPart(name, part);
      } else {
        throw new IllegalArgumentException("Invalid format: " + trimmed);
      }
    }
  }
}
