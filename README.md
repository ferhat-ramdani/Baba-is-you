<div align="center">
  
# Baba Is You

<img width="800" height="450" alt="babaisyou" src="https://github.com/user-attachments/assets/1d8a3720-89ba-4fd6-88d8-00f717fed305" />


**A rule-bending, grid-based puzzle game where you control the laws of physics.**

</div>

---

This repository contains a Java-based implementation of the puzzle game Baba Is You. The project faithfully replicates the core mechanics of the original game, where the rules governing the environment are physical text blocks that the player can push and manipulate to alter the game state.

## Documentation

Comprehensive manuals for both end-users and developers are provided in the `docs` directory:

* **User Manual**: See [docs/user.md](docs/user.md) for instructions on how to play the game, a breakdown of property behaviors, and a guide for creating custom levels.
* **Developer Manual**: See [docs/dev.md](docs/dev.md) for architectural details, the MVC design pattern implementation, and the underlying recursive collision system used in the codebase.

Try the game here : [baba-is-you.onrender.com](https://baba-is-you.onrender.com/)

## Quick Start

### Prerequisites

* Java Development Kit (JDK) 21 or higher.
* Apache Ant.

### Launching the Game

To build and run the project, open a terminal in the root directory of this repository and execute the following commands:

#### Production (Executable JAR)

1. Compile the source code and build the executable JAR file:
   ```cmd
   ant jar
   ```

2. Launch the game:
   ```cmd
   java -jar baba.jar
   ```

#### Development & Testing

To test changes quickly without packaging the game into a JAR file, use the provided helper scripts:

**On Windows:**
```cmd
run-dev.bat
```

**On macOS/Linux:**
```bash
./run-dev.sh
```

These scripts will automatically compile the source code and launch the game directly from the compiled classes.

*Note: The game must be launched from the root directory of the repository to ensure it can correctly load the `src/images/` and `src/levels/` resource directories.*

#### Web / Production Deployment (Docker)

The game can be deployed as a web application using Docker. No external dependencies (no Webswing, no VNC) are required — the game runs as a lightweight Java HTTP server and renders entirely in the browser via HTML5 Canvas.

**Requirements:** Docker

```bash
# Build the image
docker build -t baba-is-you .

# Run on port 8080
docker run -d -p 8080:8080 --name baba-is-you baba-is-you
```

Then open **http://localhost:8080** in your browser.

The game state is served as a tiny JSON payload (~1KB per move). Sprites are cached by the browser after the first load. Performance is identical to running it natively.

## Key Features

* **Dynamic Rule System:** Push text blocks together to rewrite the physics and logic of the game during runtime.
* **Custom Level Engine:** Define and load custom levels using standard text files.
* **Undo System:** Press the spacebar to rewind previous states step-by-step to the beginning of the level.
* **Modern Java:** Built utilizing modern Java 21 features including Records, Sealed Interfaces, and Pattern Matching.

## About This Project & Credits

This repository is an academic school project completed at **ESIEE Paris**. It is only a small educational replica of the original game.

Full credit and rights for the original **Baba Is You** concept, mechanics, and design belong entirely to its creator, **Arvi Teikari (Hempuli)**. Please support the official release and visit the [Baba Is You official website](https://hempuli.com/baba/) for the official game and documentation.

All images, gifs, and animations used in this project were taken from the [Baba Is You Wiki Advanced Rulebook](https://babaiswiki.fandom.com/wiki/Advanced_rulebook).

---
<div align="center">
  <i>"Baba Is You, and you are whatever you want to be."</i>
</div>
