import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints; // Import for potential use in paint
import java.awt.Color; // Import for Color class
// import java.awt.image.BufferedImage; // Not used
// import java.awt.Rectangle; // Not used
// import java.awt.Robot; // Not used
// import java.awt.Toolkit; // Not used
// import java.awt.AWTException; // Not used
import java.util.ArrayList;
import java.util.List; // Use List interface
import javax.swing.JFrame;
import javax.swing.JPanel;
// import javax.swing.JTextField; // Not used


// main class opened on startup. Responsible for game loop and high-level state.
public class main extends JPanel implements KeyListener, MouseListener {

    // --- Game Settings & State ---
	static final int FRAME_WIDTH = 800; // Use constants for size
    static final int FRAME_HEIGHT = 900; // Includes space for score maybe?
    static final int GAME_AREA_HEIGHT = 800; // Assumed square game area

	Object fruit; // Using Object class, rename if you changed it (e.g., GameObject)
	Snake snake;
	int size = 8; // Size of the game cube grid (NxN)
	ArrayList<Object> border = new ArrayList<>(); // Obstacles/borders (Using Object class)

    // Score tracking
	int [] score; // Index: level + difficulty * numLevels
	int [] highscore; // Index: level + difficulty * numLevels
	int difficulty = 0; // 0=normal, 1=hard
	int level = 0;      // 0=none, 1=level1, 2=level2, 3=big cube

    // Input Handling
	boolean [] keys = new boolean[256]; // Store current key state by key code
	boolean turnInputRegistered = false; // Ensure only one turn per snake move cycle

    // Timing
    long lastUpdateTime = System.nanoTime();
    long lastSnakeMoveTime = System.currentTimeMillis();
    final int BASE_MOVE_DELAY_MS = 150; // Initial delay between moves
    final int MAX_SCORE_SPEEDUP = 80; // Max reduction in delay (ms) due to score
    final double SPEEDUP_FACTOR = 0.25; // How much score reduces delay (lower = faster speedup)


    // Window & Menu
	JFrame frame;
	static MenuOverlay overlay = null;
	static main g; // Static reference to the game instance

    // Level Data (Consider moving to Assets or a separate file/class)
	static final int [][][] levelData = {
        // Level 1 (index 0)
		{	// x, y pairs for face 4 and 5
			{3, 1}, {4, 1}, {2, 2}, {3, 2}, {4, 2}, {5, 2}, {1, 3}, {2, 3},
            {3, 3}, {4, 3}, {5, 3}, {6, 3}, {1, 4}, {2, 4}, {3, 4}, {4, 4},
            {5, 4}, {6, 4}, {2, 5}, {3, 5}, {4, 5}, {5, 5}, {3, 6}, {4, 6}
		},
        // Level 2 (index 1)
		{   // x, y pairs applied to all 6 faces
			{0, 0}, {0, 1}, {0, 2}, {0, 5}, {0, 6}, {0, 7}, {1, 0}, {2, 0},
            {5, 0}, {6, 0}, {1, 7}, {2, 7}, {5, 7}, {6, 7}, {7, 0}, {7, 1},
            {7, 2}, {7, 5}, {7, 6}, {7, 7}
		}
	};

    // --- Constructor ---
    public main() {
        setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        setFocusable(true); // Ensure panel can receive key events
        addKeyListener(this);
        addMouseListener(this);
        init(); // Initialize game state
    }

    // --- Initialization ---
	public void init() {
        int numLevels = Assets.levels.length;
        int numDifficulties = Assets.difficulty.length;
        int totalModes = numLevels * numDifficulties; // Adjusted calculation

		score = new int[totalModes]; // Initialize score array
		highscore = Assets.load(); // Load highscores from file

        // Verify highscore array length after loading
        if (highscore.length != totalModes) {
             System.err.println("Warning: Loaded highscore length mismatch. Resetting.");
             highscore = new int[totalModes];
             Assets.save(highscore); // Save the corrected empty array
        }


		Assets.ax = 0; // Reset cube rotation
		Assets.ay = 0;

		// Start with the main menu overlay
        // Pass false because we are not dead when initializing
		changeOverlay(new MainMenu(false));

		initGame(); // Setup the game elements (snake, fruit, level)
	}

	// Reset the game elements for the current level/difficulty
	public void initGame() {
		// Determine grid size based on level
        size = (level == 3) ? 16 : 8; // Level 3 ("big cube") uses 16x16

        // Create Graphics3D object (needs to be done *before* creating snake/fruit)
        Assets.g3d = new Graphics3D(size);
        Assets.g3d.reload(); // Initial projection calculation

		// Create Snake
		snake = new Snake(size, size/2, size/2, 0); // Start snake in middle of face 0

		// Create Level Borders/Obstacles
		border = new ArrayList<>();
		if (level == 1) { // Level 1 layout
            int[][] data = levelData[0];
			for (int[] pair : data) {
				border.add(new Object(pair[0], pair[1], 4)); // Face 4
                border.add(new Object(pair[0], pair[1], 5)); // Face 5
			}
		} else if (level == 2) { // Level 2 layout
            int[][] data = levelData[1];
			for(int j = 0; j < 6; j++) { // Apply to all 6 faces
				for (int[] pair : data) {
					border.add(new Object(pair[0], pair[1], j));
				}
			}
		}
        // Level 0 (none) and 3 (big cube) have no predefined borders

		// Create initial Fruit
		fruit = new Object(size, snake, border); // Pass snake and borders for collision check

        // Reset score for the current mode
        int scoreIndex = level + difficulty * Assets.levels.length;
        if (scoreIndex >= 0 && scoreIndex < score.length) {
            score[scoreIndex] = 0;
        } else {
            System.err.println("Error: Invalid score index in initGame: " + scoreIndex);
        }

        // Reset timing and input flags
        lastSnakeMoveTime = System.currentTimeMillis();
        turnInputRegistered = false;
	}

    // --- Game Loop Update ---
	public void update() {
        long currentTimeNano = System.nanoTime();
        double deltaTime = (currentTimeNano - lastUpdateTime) / 1_000_000_000.0; // Delta time in seconds
        lastUpdateTime = currentTimeNano;

        // --- Input Handling for Cube Rotation ---
		boolean rotationChanged = false;
        double rotateSpeed = 2.0; // Radians per second
		if(keys[KeyEvent.VK_W]) { Assets.ay -= rotateSpeed * deltaTime; rotationChanged = true; } // Rotate Up (Pitch)
		if(keys[KeyEvent.VK_S]) { Assets.ay += rotateSpeed * deltaTime; rotationChanged = true; } // Rotate Down (Pitch)
		if(keys[KeyEvent.VK_A]) { Assets.ax += rotateSpeed * deltaTime; rotationChanged = true; } // Rotate Left (Yaw)
		if(keys[KeyEvent.VK_D]) { Assets.ax -= rotateSpeed * deltaTime; rotationChanged = true; } // Rotate Right (Yaw)

        // Only reload projection if rotation actually changed or in menu
		if (rotationChanged || overlay != null) {
		    Assets.g3d.reload();
        }

        // --- Game Logic (only run if not in menu) ---
		if(overlay == null) {
            // --- Snake Movement Timing ---
            long currentTimeMillis = System.currentTimeMillis();
            int scoreIndex = level + difficulty * Assets.levels.length; // Consistent index calc
            int currentScore = (scoreIndex >= 0 && scoreIndex < score.length) ? score[scoreIndex] : 0;

            // Calculate dynamic delay: faster with higher score
            int speedup = (int)(currentScore * SPEEDUP_FACTOR);
            int moveDelay = BASE_MOVE_DELAY_MS - Math.min(speedup, MAX_SCORE_SPEEDUP); // Clamp speedup
            moveDelay = Math.max(50, moveDelay); // Ensure minimum delay (e.g., 50ms)

			if(currentTimeMillis >= lastSnakeMoveTime + moveDelay) {
				lastSnakeMoveTime = currentTimeMillis; // Reset timer for next move
                turnInputRegistered = false; // Allow new turn input for the next frame

				// --- Move Snake ---
				if (snake != null) snake.move(); else return; // Safety check

                // --- Check Collisions ---
                boolean gameOver = false;
                // 1. Eat Fruit
				if(snake.eat(fruit)) {
					// Increase score
                    if (scoreIndex >= 0 && scoreIndex < score.length) {
                        score[scoreIndex]++;
                        // Check/Update highscore
					    if(score[scoreIndex] > highscore[scoreIndex]) {
						    highscore[scoreIndex]  = score[scoreIndex];
                            // Consider saving high score immediately or on game over
					    }
                    }
					// Hard mode: Turn eaten fruit into a border block
                    if(difficulty == 1 && fruit != null) {
                        // Remove fruit color before changing it
                        Assets.g3d.setColor(fruit.pos.x, fruit.pos.y, fruit.pos.z, Assets.bgColor);
						fruit.turnToBorder();
						border.add(fruit); // Add the (now border) fruit to obstacles
					}
					// Create new fruit
					fruit = new Object(size, snake, border);
				}
                // 2. Self Collision
                else if(snake.eatSelf()) {
					gameOver = true;
				}
                // 3. Border Collision
				else if(snake.eatBorder(border)) {
					gameOver = true;
				}

                // --- Handle Game Over ---
                if (gameOver) {
                    // Save highscore if it was beaten
                    int loadedHighScore = Assets.load()[scoreIndex]; // Load fresh score to compare
                    if(highscore[scoreIndex] > loadedHighScore) {
						Assets.save(highscore);
					}
                    changeOverlay(new MainMenu(true)); // Show death screen
                }
			} // End snake move block
		} // End game logic block (if overlay == null)
	} // End update()

	// --- Input Event Handlers ---
	@Override public void keyTyped(KeyEvent e) {} // Not used

	@Override
	public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < keys.length) {
		    keys[keyCode] = true; // Mark key as down
        }

        // Handle game-specific key presses (only when game is active)
		if(overlay == null) {
            // Handle snake turning input - only one turn per move cycle
            if (!turnInputRegistered) {
			    if(keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_NUMPAD4) { // Left Arrow or Numpad 4
				    if (snake != null) snake.changedir(-1); // Turn Left
                    turnInputRegistered = true;
			    } else if(keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_NUMPAD6) { // Right Arrow or Numpad 6
				    if (snake != null) snake.changedir(1); // Turn Right
                    turnInputRegistered = true;
			    }
            }
             // Pause Game (e.g., with P or Esc)
            if (keyCode == KeyEvent.VK_P || keyCode == KeyEvent.VK_ESCAPE) {
                 changeOverlay(new MainMenu(false)); // Go to menu (which pauses game)
            }

		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
		if (keyCode >= 0 && keyCode < keys.length) {
            keys[keyCode] = false; // Mark key as up
        }

        // Let the overlay handle its key release events (like starting game)
		if(overlay != null) {
			overlay.keyReleased(e);
		} else {
            // Handle key releases relevant *only* when game is active (if any)
            // Example: If space was used for something other than pause
        }
	}

	@Override public void mouseClicked(MouseEvent e) {} // Not used for game, overlay might use release
	@Override public void mouseEntered(MouseEvent e) {} // Not used
	@Override public void mouseExited(MouseEvent e) {} // Not used

	@Override
	public void mousePressed(MouseEvent e) {
		if(overlay != null)
			overlay.mousePressed(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(overlay != null)
			overlay.mouseReleased(e);
	}

	// --- Painting ---
	@Override
	public void paintComponent(Graphics g) { // Use paintComponent for Swing JPanel
        super.paintComponent(g); // Clear background (important!)
		Graphics2D g2d = (Graphics2D) g.create(); // Create a copy to avoid modifying original

        // --- Improve Rendering Quality ---
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);


		// --- Draw Background ---
		g2d.setColor(Assets.bgColor); // Use light background color
		g2d.fillRect(0, 0, getWidth(), getHeight());

		// --- Draw Score / UI Text ---
        int scoreIndex = level + difficulty * Assets.levels.length; // Consistent index calc
        int currentScore = (scoreIndex >= 0 && scoreIndex < score.length) ? score[scoreIndex] : 0;
        int currentHighScore = (scoreIndex >= 0 && scoreIndex < highscore.length) ? highscore[scoreIndex] : 0;

		g2d.setColor(Assets.textColor); // Black text
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
		g2d.drawString("Score: "+ currentScore + " / High: " + currentHighScore, 5, 20);
		g2d.drawString("Mode: "+ Assets.difficulty[difficulty] + " | Level: " + Assets.levels[level], 5, 45);


		// --- Draw 3D Cube ---
        if (Assets.g3d != null) {
		    Assets.g3d.drawCube(g2d);
        } else {
            g2d.setColor(Color.RED);
            g2d.drawString("Error: Graphics3D not initialized!", 50, 100);
        }

		// --- Draw Menu Overlay (if active) ---
		if(overlay != null) {
			overlay.paint(g2d);
		}

        g2d.dispose(); // Release graphics copy
	}

    // --- Static Methods ---
	static void changeOverlay(MenuOverlay ov) {
		overlay = ov;
        // When changing overlay, request focus back to the panel if needed
        if (g != null) g.requestFocusInWindow();
	}

    // --- Main Entry Point ---
	public static void main(String [] args) {
		g = new main(); // Create game instance (calls init)

		g.frame = new JFrame("Snake 3D - Light Theme"); // Updated title
		g.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		g.frame.setResizable(false); // Prevent resizing issues with fixed layout
		// g.frame.setUndecorated(true); // Optional: remove window borders
		g.frame.add(g); // Add JPanel to the JFrame
        g.frame.pack(); // Size the frame based on panel's preferred size
		g.frame.setLocationRelativeTo(null); // Center window
		g.frame.setVisible(true);

        // Request focus after frame is visible
        g.requestFocusInWindow();


        // --- Main Game Loop ---
		while(true) {
			g.update(); // Update game logic and input
			g.repaint(); // Request redraw (calls paintComponent)

			// Control loop speed / yield CPU
			try {
                // Target ~60 FPS (16.67ms per frame)
                // This simple sleep isn't perfect but better than busy-waiting
				Thread.sleep(16);
			}
			catch(InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupt status
                System.out.println("Game loop interrupted.");
                break; // Exit loop
            }
            catch(Exception e) {
                System.err.println("Error in game loop sleep: " + e);
                // Optionally add a short fallback sleep
                try { Thread.sleep(16); } catch (InterruptedException ignored) {}
            }
		}
        // Optional cleanup if needed when loop exits
        System.exit(0);
	}
}