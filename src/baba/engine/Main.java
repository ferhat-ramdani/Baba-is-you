package baba.engine;

import java.io.IOException;
import baba.config.GameSetter;

public class Main {
	public static void main(String[] args) throws IOException {
		GameSetter.FirstGameSetUp(args);
		var exit = false;
		while(!exit) {
			var controller = new Controller(GameSetter.gameGrid(), GameSetter.gameConfig());
			controller.start();
			if(controller.gameWin()) {
				GameSetter.nextLevelSetUp();
			} else {
				exit = true;
			}
		}
	}
}