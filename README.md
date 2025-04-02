# Cubic Snake Game

## Project Overview
The Cubic Snake Game is a 3D version of the classic snake game built using Java and Swing. The game uses a 3D projection technique to render a cube on which the snake moves. The primary components include:

- **Game Logic:** Snake movement, collision detection, fruit generation, and level management.
- **Rendering:** Converting 3D coordinates to a 2D screen using perspective projection.
- **User Interface:** A main menu, overlays, and input handling.
- **Assets Management:** Loading/saving high scores and setting theme colors.

The project adheres to Object-Oriented Programming (OOP) principles, with clear separation of concerns across different classes.

## Workflow Summary

### Initialization
- The main class (entry point) creates an instance of the game panel, sets up the JFrame, and calls the `init()` method.
- Assets initializes global properties (colors, difficulty levels, etc.) and loads high scores from a file.

### Main Menu & Overlay
- The `MainMenu` class (extending `MenuOverlay`) is displayed to the user, allowing them to start the game, change difficulty, or select levels.
- Input events (keyboard and mouse) are handled by the overlay.

### Game Setup
- On starting the game, `initGame()` in `main.java` is called.
- A new `Graphics3D` object is created to handle 3D projection.
- A `Snake` object is initialized along with obstacles (borders) and a fruit (Object).

### Game Loop
- The game loop (inside the main class) continuously updates the game state:
  - Handles cube rotation (using keys W/A/S/D).
  - Moves the snake based on timed intervals.
  - Checks collisions (with itself, borders, and fruit).
  - Updates scores and high scores.
  - Calls `repaint()` to redraw the cube and UI.

### Rendering
- The `Graphics3D` class performs 3D calculations, rotating and projecting the cube's points onto the 2D screen.
- It renders the cube’s faces, grid tiles, and applies colors for the snake, fruit, and borders.

### Collision & Game Over
- The snake’s methods (`eat`, `eatSelf`, `eatBorder`) detect collisions.
- On collision, high scores are updated via `Assets`, and the `MainMenu` is re-displayed as a death screen.

## Detailed Code Walkthrough

### Assets.java
- **Purpose:** Manages global assets such as colors, game themes, difficulty levels, and high scores. Provides methods to save and load scores from a file.
- **Key Attributes:** Static color definitions, text arrays for difficulty and levels, and a static `Graphics3D` instance.
- **Key Methods:**
  - `save(int[] score)` – Saves high scores to a file.
  - `load()` – Reads high scores from a file and initializes them if necessary.

### Graphics3D.java
- **Purpose:** Handles 3D-to-2D projection and renders the cube with its tiles, including dynamically recalculating projections as the cube rotates.
- **Key Attributes:** Array points for cube corners, a texture 3D array to store color values for each tile on each face, rotation angles, and projection parameters.
- **Key Methods:**
  - `reload()` and `update(double ax, double ay)` – Recalculates the cube's projection.
  - `drawCube(Graphics2D g)` – Renders the cube faces and grid.
  - `rotateX(double a)` and `rotateY(double a)` – Applies 3D rotations.
  - `setColor(int x, int y, int l, Color c)` – Sets tile colors for snake, fruit, or borders.

### LinkedVector.java
- **Purpose:** Implements a linked list of vectors used to represent the snake’s body segments.
- **Key Attributes:** Inherits from `Vector` and includes references `next` and `previous`.
- **Key Methods:**
  - `link(LinkedVector next)` – Links a new segment.
  - `unlink()` – Removes a segment from the chain.

### Vector.java
- **Purpose:** A simple container for 3D coordinates (x, y, z).
- **Key Attributes:** `int x, y, z`
- **Constructors:**
  - `Vector()` – Default (sets coordinates to zero).
  - `Vector(int x, int y, int z)` – Sets specific coordinates.

### Object.java
- **Purpose:** Represents generic game objects like fruit and borders. Handles spawning at random locations ensuring no overlap with the snake or other obstacles.
- **Key Attributes:**
  - `Vector pos` – Position of the object.
  - `boolean eatable` – Flag to indicate if the object can be consumed (fruit) or is an obstacle.
  - `Color color` – The color used to render the object.
- **Key Methods:**
  - Two constructors:
    - One for creating a fruit (randomly positioned).
    - One for creating an obstacle (with a given position).
  - `onObstacle(List<Object> obstacles)` – Checks if the object overlaps with any obstacles.
  - `onSnake(Snake snake)` – Checks if it overlaps with the snake.
  - `isEaten(int x, int y, int l)` – Determines if the snake’s head occupies the same position.
  - `turnToBorder()` – Converts a fruit to a border object when consumed in hard mode.

### Snake.java
- **Purpose:** Contains all logic related to the snake's behavior. Manages movement, turning, growing, and collision detection.
- **Key Attributes:**
  - `LinkedVector head` and `tail` – Represent the snake’s body.
  - `int curLength` and `length` – Track current and desired length.
  - `int[][] diruse` – Encodes movement directions (up, right, down, left).
  - `int dir` – Current moving direction.
- **Key Methods:**
  - `move()` – Computes the new head position based on the current direction, handles face transitions when the snake moves off the edge of a face, updates the snake’s body by removing the tail if not growing, and draws the new head.
  - `changedir(int turn)` – Updates the direction (with modulo arithmetic for wrap-around).
  - `eat(Object fruit)` – Checks if the snake has consumed a fruit and increases length.
  - `eatSelf()` – Detects if the snake collides with its own body.
  - `eatBorder(List<Object> obstacles)` – Checks collision with obstacles.

### MainMenu.java & MenuOverlay.java
- **Purpose:** `MenuOverlay` is an abstract class that defines how overlays (like the main menu or pause screens) should behave. `MainMenu` extends `MenuOverlay` to implement the actual menu display and user interaction logic.
- **Key Attributes in MainMenu:**
  - `boolean isDead` – Indicates if the current overlay is shown after a game over.
  - `int difficulty, level` – Current selections.
  - Timing variables to manage input delay.
- **Key Methods in MainMenu:**
  - `keyReleased(KeyEvent e)` – Handles keyboard inputs (e.g., starting the game, cycling difficulty/level).
  - `mousePressed(MouseEvent e)` and `mouseReleased(MouseEvent e)` – Handle mouse input for button presses.
  - `paint(Graphics2D g2d)` – Renders the menu, buttons, and game status messages.

### main.java
- **Purpose:** Acts as the entry point of the application. Sets up the game window and starts the main game loop.
- **Key Attributes:** Constants for frame dimensions and game area, instances of `Snake`, `Graphics3D` (via `Assets.g3d`), `MenuOverlay`, and collections for borders, timing variables, and input state arrays.
- **Key Methods:**
  - `init()` – Initializes game state, loads high scores, sets initial cube rotations, and displays the main menu.
  - `initGame()` – Resets the game elements (snake, fruit, borders) based on the chosen level and difficulty.
  - `update()` – Handles input for cube rotation and snake movement, updates game logic based on timing (e.g., moves snake at dynamic intervals), checks collisions (fruit, self, borders), and handles game over by changing the overlay.
  - `paintComponent(Graphics g)` – Overrides the Swing `JPanel` painting method, clears the screen, draws UI elements (score, mode, level), and delegates rendering to `Graphics3D` and the active `MenuOverlay`.
  - Main loop in `main()` – Runs continuously to update and repaint the game, aiming for ~60 FPS.
