package baba.engine;

import java.io.IOException;
import baba.config.GameSetter;
import baba.web.WebServer;

public class Main {
	public static void main(String[] args) throws IOException {
		// Web mode: java -jar baba.jar --web [port]
		if (args != null && args.length > 0 && "--web".equals(args[0])) {
			var port = 8080;
			// Render (and most PaaS) set a PORT env var — always prefer it
			var envPort = System.getenv("PORT");
			if (envPort != null) {
				try { port = Integer.parseInt(envPort); } catch (NumberFormatException ignored) {}
			} else if (args.length > 1) {
				try { port = Integer.parseInt(args[1]); } catch (NumberFormatException ignored) {}
			}
			var server = new WebServer(port);
			server.start();
			// Block forever
			var lock = new Object();
			synchronized (lock) {
				try { lock.wait(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
			}
			return;
		}

		// Normal desktop mode
		boolean hasLevelArg = false;
		if (args != null) {
			for (String arg : args) {
				if (arg.equals("--levels") || arg.equals("--level")) {
					hasLevelArg = true;
					break;
				}
			}
		}

		while (true) {
			if (!hasLevelArg) {
				var menu = new MenuController();
				args = menu.show();
				if (args == null) {
					System.exit(0);
				}
			}

			GameSetter.FirstGameSetUp(args);
			var exit = false;
			while (!exit) {
				var controller = new Controller(GameSetter.gameGrid(), GameSetter.gameConfig());
				controller.start();
				if (controller.isReturnToMenu()) {
					hasLevelArg = false;
					exit = true;
				} else if (controller.gameWin()) {
					GameSetter.nextLevelSetUp();
				} else {
					exit = true;
					System.exit(0);
				}
			}
		}
	}
}