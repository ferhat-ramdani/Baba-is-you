package baba.web;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import com.sun.net.httpserver.*;

import baba.config.Config;
import baba.config.GameSetter;
import baba.grid.*;
import baba.names.PropertyWord;

/**
 * Lightweight HTTP server that serves game state as JSON and handles keyboard input via REST.
 * The browser renders the game using HTML5 Canvas — no frame streaming, just tiny JSON state updates.
 */
public class WebServer {

    private final HttpServer server;
    private volatile Grid grid;
    private volatile Config config;
    private volatile boolean win = false;
    private volatile boolean returnToMenu = false;
    private volatile String[] nextArgs = null;
    private volatile String phase = "menu"; // "menu" | "game" | "win" | "over"

    // Menu state
    private volatile java.util.List<String> menuFolders = new java.util.ArrayList<>();
    private volatile java.util.List<String> menuLevels = new java.util.ArrayList<>();
    private volatile int menuSelectedIndex = 0;
    private volatile String menuState = "FOLDER"; // "FOLDER" | "LEVEL"
    private volatile String selectedFolder = null;

    // State version counter so client knows when to refresh
    private final AtomicLong stateVersion = new AtomicLong(0);

    public WebServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        setupRoutes();
    }

    private void setupRoutes() {
        // Serve main HTML frontend
        server.createContext("/", exchange -> {
            if (!exchange.getRequestURI().getPath().equals("/")) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            String html = loadResource("/baba/web/index.html");
            byte[] bytes = html.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        });

        // Serve sprite images directly
        server.createContext("/sprites/", exchange -> {
            String path = exchange.getRequestURI().getPath(); // e.g. /sprites/blocks/BABA.gif
            String relPath = path.substring("/sprites/".length());
            var userDir = System.getProperty("user.dir");
            var filePath = Path.of(userDir, "src", "images", relPath);
            if (!Files.exists(filePath)) {
                exchange.sendResponseHeaders(404, -1);
                return;
            }
            byte[] bytes = Files.readAllBytes(filePath);
            exchange.getResponseHeaders().set("Content-Type", "image/gif");
            exchange.getResponseHeaders().set("Cache-Control", "public, max-age=3600");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        });

        // Returns current game state as JSON
        server.createContext("/api/state", exchange -> {
            addCors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String json = buildStateJson();
            byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        });

        // Accepts keyboard input: body = "UP" | "DOWN" | "LEFT" | "RIGHT" | "SPACE" | "ESCAPE" | "M" | "ENTER"
        server.createContext("/api/move", exchange -> {
            addCors(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String key = new String(exchange.getRequestBody().readAllBytes()).trim();
            handleKey(key);
            String json = buildStateJson();
            byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.getResponseBody().close();
        });
    }

    private void addCors(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private synchronized void handleKey(String key) {
        if ("menu".equals(phase)) {
            handleMenuKey(key);
        } else if ("game".equals(phase)) {
            handleGameKey(key);
        } else if ("win".equals(phase)) {
            if ("ENTER".equals(key)) {
                // Advance to next level
                try {
                    GameSetter.nextLevelSetUp();
                    grid = GameSetter.gameGrid();
                    config = GameSetter.gameConfig();
                    try { grid.searchRule(); } catch (IOException e) { e.printStackTrace(); }
                    grid.saveMove();
                    phase = "game";
                    stateVersion.incrementAndGet();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if ("ESCAPE".equals(key)) {
                phase = "menu";
                loadMenuFolders();
                stateVersion.incrementAndGet();
            }
        } else if ("over".equals(phase)) {
            if ("ENTER".equals(key) || "ESCAPE".equals(key)) {
                phase = "menu";
                loadMenuFolders();
                stateVersion.incrementAndGet();
            }
        }
    }

    private void handleMenuKey(String key) {
        var list = "FOLDER".equals(menuState) ? menuFolders : menuLevels;
        switch (key) {
            case "UP" -> {
                menuSelectedIndex--;
                if (menuSelectedIndex < 0) menuSelectedIndex = list.size() - 1;
                stateVersion.incrementAndGet();
            }
            case "DOWN" -> {
                menuSelectedIndex++;
                if (menuSelectedIndex >= list.size()) menuSelectedIndex = 0;
                stateVersion.incrementAndGet();
            }
            case "ENTER" -> {
                if ("FOLDER".equals(menuState)) {
                    if (!menuFolders.isEmpty()) {
                        selectedFolder = menuFolders.get(menuSelectedIndex);
                        loadMenuLevels(selectedFolder);
                        menuState = "LEVEL";
                        menuSelectedIndex = 0;
                        stateVersion.incrementAndGet();
                    }
                } else {
                    if (!menuLevels.isEmpty()) {
                        String level = menuLevels.get(menuSelectedIndex);
                        nextArgs = new String[]{"--levels", selectedFolder, "--level", level};
                        startGame();
                    }
                }
            }
            case "ESCAPE" -> {
                if ("LEVEL".equals(menuState)) {
                    menuState = "FOLDER";
                    menuSelectedIndex = menuFolders.indexOf(selectedFolder);
                    if (menuSelectedIndex < 0) menuSelectedIndex = 0;
                    selectedFolder = null;
                    stateVersion.incrementAndGet();
                }
            }
        }
    }

    private void handleGameKey(String key) {
        if (grid == null) return;
        boolean moved = false;
        switch (key) {
            case "UP" -> { grid.moveYou(Direction.UP); moved = true; }
            case "DOWN" -> { grid.moveYou(Direction.DOWN); moved = true; }
            case "LEFT" -> { grid.moveYou(Direction.LEFT); moved = true; }
            case "RIGHT" -> { grid.moveYou(Direction.RIGHT); moved = true; }
            case "SPACE" -> { grid.undoMove(); moved = true; }
            case "ESCAPE" -> { phase = "over"; stateVersion.incrementAndGet(); return; }
            case "M" -> {
                phase = "menu";
                loadMenuFolders();
                stateVersion.incrementAndGet();
                return;
            }
        }
        if (moved) {
            grid.checkRule(PropertyWord.SINK);
            grid.checkRule(PropertyWord.YOU, PropertyWord.DEFEAT);
            grid.checkRule(PropertyWord.MELT, PropertyWord.HOT);
            grid.checkRule(PropertyWord.STICK);
            if (grid.checkRule(PropertyWord.YOU, PropertyWord.WIN)) {
                phase = "win";
            }
            try { grid.searchRule(); } catch (IOException e) { e.printStackTrace(); }
            grid.saveMove();
            stateVersion.incrementAndGet();
        }
    }

    private void startGame() {
        try {
            // 800x600 is just used for tile-size math; browser renders at actual viewport size
            GameSetter.WebGameSetUp(nextArgs, 800, 600);
            grid = GameSetter.gameGrid();
            config = GameSetter.gameConfig();
            try { grid.searchRule(); } catch (IOException e) { e.printStackTrace(); }
            grid.saveMove();
            phase = "game";
            stateVersion.incrementAndGet();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String buildStateJson() {
        long v = stateVersion.get();
        var sb = new StringBuilder();
        sb.append("{\"version\":").append(v)
          .append(",\"phase\":\"").append(phase).append("\"");

        if ("menu".equals(phase)) {
            sb.append(",\"menuState\":\"").append(menuState).append("\"");
            sb.append(",\"selectedIndex\":").append(menuSelectedIndex);
            sb.append(",\"folders\":[");
            appendStringArray(sb, menuFolders);
            sb.append("],\"levels\":[");
            appendStringArray(sb, menuLevels);
            sb.append("]");
            if (selectedFolder != null) sb.append(",\"selectedFolder\":\"").append(selectedFolder).append("\"");
        } else if ("game".equals(phase) && grid != null) {
            sb.append(",\"rows\":").append(config.rows());
            sb.append(",\"cols\":").append(config.columns());
            sb.append(",\"items\":[");
            boolean first = true;
            for (var entry : grid.itemsByTile().entrySet()) {
                var tile = entry.getKey();
                for (var item : entry.getValue()) {
                    if (!first) sb.append(",");
                    first = false;
                    String spritePath = getSpritePath(item);
                    sb.append("{\"r\":").append(tile.row())
                      .append(",\"c\":").append(tile.column())
                      .append(",\"s\":\"").append(spritePath).append("\"")
                      .append("}");
                }
            }
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }

    private void appendStringArray(StringBuilder sb, java.util.List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(list.get(i)).append("\"");
        }
    }

    private String getSpritePath(baba.grid.Item item) {
        return switch (item) {
            case baba.grid.Block block -> "blocks/" + block.name() + ".gif";
            case baba.grid.Word word -> switch (word.name()) {
                case baba.names.BlockWord bw -> "texts/blocks/" + bw + ".gif";
                case baba.names.OperatorWord ow -> "texts/operators/" + ow + ".gif";
                case baba.names.PropertyWord pw -> "texts/properties/" + pw + ".gif";
                default -> "blocks/BABA.gif";
            };
        };
    }

    private String loadResource(String path) {
        try (var stream = WebServer.class.getResourceAsStream(path)) {
            if (stream == null) {
                // fallback: load from filesystem
                var userDir = System.getProperty("user.dir");
                return Files.readString(Path.of(userDir, "src", path));
            }
            return new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "<h1>Error loading resource: " + path + "</h1>";
        }
    }

    private void loadMenuFolders() {
        menuFolders.clear();
        menuLevels.clear();
        menuState = "FOLDER";
        menuSelectedIndex = 0;
        selectedFolder = null;
        var userDir = System.getProperty("user.dir");
        var levelsPath = Path.of(userDir, "src", "levels");
        if (Files.exists(levelsPath)) {
            try (var stream = Files.list(levelsPath)) {
                stream.filter(Files::isDirectory)
                      .map(p -> p.getFileName().toString())
                      .filter(name -> !name.equals("default"))
                      .forEach(menuFolders::add);
            } catch (IOException e) { e.printStackTrace(); }
        }
        menuFolders.sort((a, b) -> {
            if (a.equals("original")) return -1;
            if (b.equals("original")) return 1;
            return a.compareTo(b);
        });
    }

    private void loadMenuLevels(String folder) {
        menuLevels.clear();
        var userDir = System.getProperty("user.dir");
        var folderPath = Path.of(userDir, "src", "levels", folder);
        if (Files.exists(folderPath)) {
            try (var stream = Files.list(folderPath)) {
                stream.filter(p -> p.toString().endsWith(".txt"))
                      .forEach(p -> {
                          var name = p.getFileName().toString();
                          menuLevels.add(name.substring(0, name.length() - 4));
                      });
            } catch (IOException e) { e.printStackTrace(); }
        }
        menuLevels.sort((a, b) -> {
            try { return Integer.compare(Integer.parseInt(a), Integer.parseInt(b)); }
            catch (NumberFormatException e) { return a.compareTo(b); }
        });
    }

    public void start() {
        loadMenuFolders();
        server.start();
        System.out.println("Baba Is You web server started on http://0.0.0.0:" + server.getAddress().getPort());
    }

    public void stop() {
        server.stop(0);
    }
}
