package baba.names;

/**
 * An enumeration representing block words for word names.
 */
public enum BlockWord implements WordName {

    T_BABA,
    T_FLAG,
    T_FLOWER,
    T_LAVA,
    T_ROCK,
    T_SKULL,
    T_TILE,
    T_WALL,
    T_WATER,
    T_GLUE;

    /**
     * Checks if the word is a block word.
     *
     * @return true if the word is a block word, false otherwise.
     */
    @Override
    public boolean isBlockWord() {
        return true;
    }

    /**
     * Retrieves the corresponding block name for this block word.
     *
     * @return the corresponding BlockName.
     */
    public BlockName blockName() {
        return switch (this) {
            case T_BABA -> BlockName.BABA;
            case T_FLAG -> BlockName.FLAG;
            case T_FLOWER -> BlockName.FLOWER;
            case T_LAVA -> BlockName.LAVA;
            case T_ROCK -> BlockName.ROCK;
            case T_SKULL -> BlockName.SKULL;
            case T_TILE -> BlockName.TILE;
            case T_WALL -> BlockName.WALL;
            case T_WATER -> BlockName.WATER;
            case T_GLUE -> BlockName.GLUE;
        };
    }
}
