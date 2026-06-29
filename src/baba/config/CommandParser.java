package baba.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import baba.names.BlockRule;
import baba.names.BlockWord;
import baba.names.EmptyRule;
import baba.names.OperatorWord;
import baba.names.PropRule;
import baba.names.PropertyWord;
import baba.names.Rule;
import baba.names.SpriteName;
import baba.names.WordName;

/**
 * CommandParser is a utility class that parses command-line arguments and
 * updates the Config object accordingly.
 */
public class CommandParser {
  /**
   * The command-line arguments passed to the parser.
   */
  private final String[] args;
  /**
   * A map of flags to their corresponding parameters.
   */
  private final Map<Flags, List<String>> command;
  /**
   * The Config object to be updated based on the parsed command.
   */
  private Config config;

  /**
   * Constructs a new CommandParser instance with the given command-line arguments
   * and Config object.
   * 
   * @param args   the command-line arguments
   * @param config the Config object to be updated
   */
  public CommandParser(String[] args, Config config) {
    this.args = Objects.requireNonNull(args);
    command = new HashMap<Flags, List<String>>();
    this.config = config;
  }

  /**
   * Parses the command-line arguments and updates the Config object accordingly.
   * 
   * @return true if the parsing was successful, false otherwise
   */
  public boolean parse() {
    try {
      command.putAll(retrieveCommand());
      executeCommand(command);
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
      return false;
    }
    return true;
  }

  /**
   * Returns the updated Config object.
   * 
   * @return the updated Config object
   */
  public Config config() {
    return config;
  }

  private Map<Flags, List<String>> retrieveCommand() {
    var command = new HashMap<Flags, List<String>>();
    for (var i = 0; i < args.length && args[i].startsWith("--"); i++) {
      try {
        var flag = Flags.valueOf(args[i].substring(2));
        switch (flag) {
          case levels -> command.put(flag, List.of(args[++i]));
          case level -> command.put(flag, List.of(args[++i] + ".txt"));
          case execute -> command.put(flag, List.of(args[++i], args[++i], args[++i]));
        }
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Unrecognized flag " + args[i] + " in given command");
      }
    }
    return Map.copyOf(command);
  }
  
  private Rule parseSpecialRule(List<String> wordList) {
    if (wordList.size() != 3) {
      throw new IllegalArgumentException("The rule must contain exactly three words.");
    }
    List<WordName> words = wordList.stream()
      .map(LevelParser::nameFromText)
      .filter(SpriteName::isWord)
      .map(w -> (WordName)w)
      .toList();
    if(words.size() != 3) {
      throw new IllegalArgumentException("Wrong words");
    }
    if(words.get(0).isBlockWord() && words.get(1).isOperatorWord()) {
      if(words.get(2).isBlockWord()) {
        return new BlockRule((BlockWord)words.get(0), (OperatorWord)words.get(1), (BlockWord)words.get(2));
      } else if(words.get(2).isPropertyWord()) {
        return new PropRule((BlockWord)words.get(0), (OperatorWord)words.get(1), (PropertyWord)words.get(2));
      }
    } else {
      throw new IllegalArgumentException("Wrong words");
    }
    return new EmptyRule();
  }

  private void executeCommand(Map<Flags, List<String>> command) {
    command.forEach((flag, params) -> {
      switch (flag) {
        case level -> config = config.updateLevel(config.level().resolveSibling(params.getFirst()));
        case levels -> config = config.updateLevel(
          config.level().getParent().resolveSibling(params.getFirst()).resolve(config.level().getFileName()));
        case execute -> {
          config = config.updateSpecialRule(parseSpecialRule(params));
        }
      }
    });
  }
}
