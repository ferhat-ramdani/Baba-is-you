package baba.config;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import baba.grid.Block;
import baba.grid.Grid;
import baba.grid.Tile;
import baba.grid.Word;
import baba.names.BlockName;
import baba.names.SpriteName;
import baba.names.WordName;
import baba.view.ImageLoader;

/**
 * Factory class responsible for creating and placing items on the grid.
 */
public record Factory() {

	/**
	 * Puts a single tile with the given name onto the grid.
	 *
	 * @param grid The grid to put the tile on.
	 * @param name The name of the sprite to place.
	 * @param tile The tile to place the sprite on.
	 * @throws IOException If there is an error loading the image.
	 */
	public static void put(Grid grid, SpriteName name, Tile tile) throws IOException {
		Objects.requireNonNull(name);
		Objects.requireNonNull(tile);
		var item = switch (name) {
		case BlockName blockname -> new Block(ImageLoader.load(blockname), blockname, tile);
		case WordName textName -> new Word(ImageLoader.load(textName), textName, tile);
		};
		grid.addItem(item);
	}

	/**
	 * Puts multiple tiles with the given name onto the grid.
	 *
	 * @param grid  The grid to put the tiles on.
	 * @param name  The name of the sprite to place.
	 * @param tiles The tiles to place the sprite on.
	 * @throws IOException If there is an error loading the image.
	 */
	public static void put(Grid grid, SpriteName name, List<Tile> tiles) throws IOException {
		Objects.requireNonNull(name);
		Objects.requireNonNull(tiles);
		for (var tile : tiles) {
			put(grid, name, tile);
		}
	}

	/**
	 * Puts and links a series of tiles with the given name onto the grid.
	 *
	 * @param grid     The grid to put the tiles on.
	 * @param name     The name of the sprite to place.
	 * @param tileList The list of tiles to place the sprite on.
	 * @throws IOException              If there is an error loading the image.
	 * @throws IllegalArgumentException If the tile list is empty.
	 */
	public static void putAndLink(Grid grid, SpriteName name, List<Tile> tileList) throws IOException {
		if (tileList.isEmpty()) {
			throw new IllegalArgumentException("Must give coordinates for at least one tile");
		}
		put(grid, name, tileList.get(0));
		for (var i = 0; i < tileList.size() - 1; i++) {
			var curTile = tileList.get(i);
			var nextTile = tileList.get(i+1);
			for (var j = loopInitValue(curTile, nextTile, Tile::row, Integer::sum); j <= nextTile.row(); j++) {
				for (var k = loopInitValue(curTile, nextTile, Tile::column, Integer::sum); k <= nextTile.column(); k++) {
					put(grid, name, new Tile(j, k));
				}
			}
			for (var j = loopInitValue(curTile, nextTile, Tile::row, (a,b) -> a-b); j >= nextTile.row(); j--) {
				for (var k = loopInitValue(curTile, nextTile, Tile::column, (a,b) -> a-b); k >= nextTile.column(); k--) {
					put(grid, name, new Tile(j, k));
				}
			}
		}
	}
	
	private static int loopInitValue(Tile tile1, Tile tile2, Function<Tile, Integer> orientation , BinaryOperator<Integer> op) {
		int v1 = orientation.apply(tile1);
		int v2 = orientation.apply(tile2);
		return v1 == v2 ? v1 : op.apply(v1, 1);
	}

}