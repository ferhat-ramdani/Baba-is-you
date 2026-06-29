package baba.grid;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import baba.config.Config;
import baba.names.*;

/**
 * Represents a grid in the game, managing items, behaviors, and rules.
 */
public class Grid {
  private Config config;
  private final HashMap<Tile, ArrayList<Item>> itemsByTile;
  private final HashMap<BlockName, Behavior> behaviors;
  private final ArrayList<Item> blockedItems;
  private final ArrayList<AtomicMove> atomics;
  private final Deque<List<AtomicMove>> moves;
  private final ArrayList<Word> operators;
  private final Set<BlockName> youNames;

  /**
   * Constructs a new Grid with the specified configuration.
   *
   * @param config the configuration for the grid
   */
  public Grid(Config config) {
    this.config = Objects.requireNonNull(config);
    itemsByTile = new HashMap<>();
    behaviors = new HashMap<>();
    blockedItems = new ArrayList<>();
    atomics = new ArrayList<>();
    moves = new ArrayDeque<>();
    operators = new ArrayList<>();
    youNames = new HashSet<>();
  }

  /**
   * Updates the grid configuration.
   *
   * @param config the new configuration
   */
  public void updateConfig(Config config) {
    this.config = config;
  }

  /**
   * Adds an item to the grid.
   *
   * @param item the item to be added
   */
  public void addItem(Item item) {
    Objects.requireNonNull(item);
    itemsByTile.computeIfAbsent(item.tile(), k -> new ArrayList<>()).add(item);
    switch (item) {
      case Block block -> behaviors.computeIfAbsent(block.name(), k -> new Behavior());
      case Word word -> {
        if (word.name().isOperatorWord()) {
          operators.add(word);
        }
      }
    }
  }
  
  /**
   * Moves all items with the "YOU" property in a specified direction.
   *
   * @param direction the direction in which to move the items
   */
  public void moveYou(Direction direction) {
    youNames.stream()
      .map(this::blocksByName)
      .flatMap(Collection::stream)
      .forEach(you -> moveItem(you, direction));
  }

  /**
   * Saves the current move.
   */
  public void saveMove() {
    if (!atomics.isEmpty()) {
      moves.push(List.copyOf(atomics));
      atomics.clear();
    }
  }

  /**
   * Undoes the last move.
   */
  public void undoMove() {
    if (!moves.isEmpty()) {
      moves.poll().forEach(atomic -> updateItem(atomic.newItem(), atomic.prevItem()));
    }
  }

  /**
   * Checks a rule involving two properties.
   *
   * @param yin  the first property
   * @param yang the second property
   * @return true if the rule is satisfied, false otherwise
   */
  public boolean checkRule(PropertyWord yin, PropertyWord yang) {
    var removedItems = new ArrayList<Item>();
    for (var tileItems : itemsByTile.values()) {
      if (itemsHaveProperty(tileItems, yin) && itemsHaveProperty(tileItems, yang)) {
        switch (yang) {
          case SINK -> {
            if (tileItems.size() > 1) {
              removedItems.addAll(tileItems);
            }
          }
          case DEFEAT -> {
            for (var item : tileItems) {
              if (!getItemBehavior(item).hasProperty(yang)
                  && getItemBehavior(item).hasProperty(PropertyWord.YOU)) {
                removedItems.add(item);
              }
            }
          }
          case HOT -> {
            for (var item : tileItems) {
              if (!getItemBehavior(item).hasProperty(yang)
                  && getItemBehavior(item).hasProperty(PropertyWord.MELT)) {
                removedItems.add(item);
              }
            }
          }
          case WIN -> {
            return true;
          }
          case STICK -> {
            blockedItems.addAll(tileItems.stream()
              .filter(item -> !getItemBehavior(item).hasProperty(yang))
              .toList());
          }
          default -> {}
        }
      }
    }
    removeItems(removedItems);
    return false;
  }

  /**
   * Checks a rule involving a single property.
   *
   * @param property the property to check
   * @return true if the rule is satisfied, false otherwise
   */
  public boolean checkRule(PropertyWord property) {
    return checkRule(null, property);
  }

  /**
   * Gets the items organized by their tile positions.
   *
   * @return an unmodifiable map of items by tile positions
   */
  public Map<Tile, ArrayList<Item>> itemsByTile() {
    return Map.copyOf(itemsByTile);
  }

  private <T, U> BiFunction<T, ArrayList<U>, ArrayList<U>> filterWithParameter(U item) {
    return (k, v) -> Stream
      .concat(v.stream().filter(item::equals).skip(1), 
              v.stream().filter(e -> !e.equals(item)))
      .collect(Collectors.toCollection(ArrayList::new));
  }

  private void removeItem(Item item) {
    Objects.requireNonNull(item);
    itemsByTile.computeIfPresent(item.tile(), filterWithParameter(item));
    itemsByTile.remove(item.tile(), List.of());
    if (item.isWord()) {
      if (((Word) item).name().isOperatorWord()) {
        operators.remove((Word) item);
      }
    }
  }

  private List<Block> blocksByName(BlockName name) {
    return itemsByTile.values().stream()
      .flatMap(Collection::stream)
      .map(i -> blockWithName(i, name))
      .filter(Optional::isPresent)
      .map(Optional::get)
      .toList();
  }

  private Optional<Block> blockWithName(Item item, BlockName name) {
    return switch (item) {
      case Word word -> Optional.ofNullable(null);
      case Block block -> block.name().equals(name) ? Optional.of(block) : Optional.ofNullable(null);
    };
  }

  private Behavior getItemBehavior(Item item) {
    Objects.requireNonNull(item);
    return switch (item) {
      case Word word -> new Behavior(PropertyWord.PUSH);
      case Block block -> behaviors.get(block.name());
    };
  }

  private void removeItems(List<Item> items) {
    for (var item : items) {
      removeItem(item);
      atomics.addFirst(new AtomicMove(item, item));
    }
  }

  private void updateItem(Item item, Item newItem) {
    Objects.requireNonNull(item);
    Objects.requireNonNull(newItem);
    if (!item.equals(newItem) || itemsByTile.get(item.tile()) == null
        || !(itemsByTile.get(item.tile()).contains(item))) {
      removeItem(item);
      addItem(newItem);
    }
  }

  private void moveTileItems(Tile tile, Direction direction) {
    Objects.requireNonNull(tile);
    for (var item : List.copyOf(itemsByTile.get(tile))) {
      if (getItemBehavior(item).hasProperty(PropertyWord.PUSH)) {
        var newItem = item.move(direction);
        updateItem(item, newItem);
        atomics.addFirst(new AtomicMove(item, newItem));
      }
    }
  }

  private boolean itemsHaveProperty(ArrayList<Item> list, PropertyWord property) {
    return containsProp1WithoutProp2(list, property, null);
  }

  private boolean containsProp1WithoutProp2(ArrayList<Item> list, PropertyWord prop1, PropertyWord prop2) {
    if (list != null) {
      for (var item : list) {
        if (getItemBehavior(item).hasProperty(prop1) && (prop2 == null || !getItemBehavior(item).hasProperty(prop2))) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean pushRecursively(Tile tile, Direction direction) {
    var list = itemsByTile.get(tile);
    if (list == null) {
      return true;
    }
    if (!list.stream().filter(item -> blockedItems.contains(item)).toList().isEmpty()) {
      return false;
    }
    if (containsProp1WithoutProp2(list, PropertyWord.STOP, PropertyWord.YOU)) {
      return false;
    }
    if (!itemsHaveProperty(list, PropertyWord.PUSH)) {
      return true;
    }
    if (itemsHaveProperty(list, PropertyWord.YOU)) {
      return true;
    }
    if (tile.touchesBorder(direction, config)) {
      return false;
    }
    if (pushRecursively(tile.adjacentTile(direction), direction)) {
      moveTileItems(tile, direction);
      return true;
    }
    return false;
  }
  
  private void moveItem(Item item, Direction direction) {
    Objects.requireNonNull(item);
    if (itemsByTile.containsKey(item.tile()) && !item.tile().touchesBorder(direction, config)
        && !blockedItems.contains(item)) {
      if (pushRecursively(item.tile().adjacentTile(direction), direction)) {
        var newItem = item.move(direction);
        updateItem(item, newItem);
        atomics.addFirst(new AtomicMove(item, newItem));
      }
    }
  }

  private void replaceSprite(BlockName oldName, BlockName newName) throws IOException {
    Objects.requireNonNull(oldName);
    Objects.requireNonNull(newName);
    var oldBlocks = blocksByName(oldName);
    for (var block : oldBlocks) {
      var newBlock = ((Block) block).changeSprite(newName);
      updateItem(block, newBlock);
      atomics.addFirst(new AtomicMove(block, newBlock));
    }
  }

  private void addProperty(BlockName name, PropertyWord property) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(property);
    behaviors.computeIfAbsent(name, k -> new Behavior()).addProperties(property);
    if (property.equals(PropertyWord.YOU)) {
      youNames.add(name);
    }
  }
  
  public void searchRule() throws IOException {
    if (!operators.isEmpty()) {
      behaviors.replaceAll((k, n) -> new Behavior());
      youNames.clear();
      searchRuleHorizontally();
      searchRuleVertically();
    }
    applySpecialRule(config.specialRule());
  }

  private PropertyWord returnWord(List<Item> items) {
    if (items == null) {
      return null;
    }
    for (var item : items) {
      if (item.isWord()) {
        var word = (Word) item;
        if (word.name().isPropertyWord())
          return (PropertyWord) word.name();
      }
    }
    return null;
  }

  private BlockWord returnBlock(List<Item> items) {
    if (items == null) {
      return null;
    }
    for (var item : items) {
      if (item.isWord()) {
        var word = (Word) item;
        if (word.name().isBlockWord()) {
          return (BlockWord) word.name();
        }
      }
    }
    return null;
  }

  private void searchRuleByDir(Direction dir1, Direction dir2) throws IOException {
    for (Word operator : operators) {
      if (!(operator.tile().touchesBorder(dir1, config)
          || operator.tile().touchesBorder(dir2, config))) {
        var itemsFirst = itemsByTile().get(operator.tile().adjacentTile(dir1));
        var itemsSecond = itemsByTile().get(operator.tile().adjacentTile(dir2));
        var propertySecond = returnWord(itemsSecond);
        var blockWordFirst = returnBlock(itemsFirst);
        var blockWordSecond = returnBlock(itemsSecond);

        if (propertySecond != null && blockWordFirst != null) {
          addProperty(blockWordFirst.blockName(), propertySecond);
          if (propertySecond.equals(PropertyWord.YOU)) {
            youNames.add(blockWordFirst.blockName());
          }
        } else if (blockWordFirst != null && blockWordSecond != null) {
          replaceSprite(blockWordFirst.blockName(), blockWordSecond.blockName());
        }
      }
    }
  }

  private void applySpecialRule(Rule specialRule) throws IOException {
    switch (specialRule) {
      case PropRule propRule -> {
        addProperty(propRule.block().blockName(), propRule.prop());
        if (propRule.prop().equals(PropertyWord.YOU)) {
          youNames.add(propRule.block().blockName());
        }
      }
      case BlockRule blockRule -> {
        replaceSprite(blockRule.block1().blockName(), blockRule.block2().blockName());
      }
      case EmptyRule emptyRule -> {}
    }
  }

  private void searchRuleHorizontally() throws IOException {
    searchRuleByDir(Direction.LEFT, Direction.RIGHT);
  }

  private void searchRuleVertically() throws IOException {
    searchRuleByDir(Direction.UP, Direction.DOWN);
  }
}
