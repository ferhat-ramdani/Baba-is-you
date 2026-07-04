package baba.engine;

import java.io.IOException;
import baba.config.GameSetter;

public class Main {
	public static void main(String[] args) throws IOException {
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