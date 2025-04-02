import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Color; // Import Color if needed for explicit colors

public class MainMenu extends MenuOverlay {
	boolean isDead;
	long lastT = System.currentTimeMillis();
	long startT; // Time this overlay was created.
	int pressed = -1; // Shows which button is currently pressed by the mouse.
	int difficulty = 0;
	int level = 0;

	public MainMenu(boolean isDead) {
		this.isDead = isDead;
		startT = System.currentTimeMillis();
        // Ensure difficulty/level are initialized from the main game state
        if (main.g != null) {
            this.difficulty = main.g.difficulty;
            this.level = main.g.level;
        }
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(isDead) {
			if(System.currentTimeMillis() - startT < 1000) // Delay after death
				return;
			// Allow rotation keys even on death screen
			if(e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_S ||
			   e.getKeyCode() == KeyEvent.VK_A || e.getKeyCode() == KeyEvent.VK_D)
				return;
			// Any other key restarts
			main.g.init(); // Re-initialize the whole game state
			// isDead = false; // This will be handled by creating a new MainMenu(false) in init()
			return; // Important: exit after init()
		}

		// --- In Main Menu (not dead) ---
		switch(e.getKeyCode()) {
			case KeyEvent.VK_SPACE:
			case KeyEvent.VK_ENTER: // Allow Enter too
				main.changeOverlay(null); // Start game
				break;
			case KeyEvent.VK_G: // Cycle Difficulty
				difficulty = (difficulty + 1) % Assets.difficulty.length;
				main.g.difficulty = difficulty;
				// No need to initGame() just for difficulty change
				break;
			case KeyEvent.VK_L: // Cycle Level
				level = (level + 1) % Assets.levels.length;
				main.g.level = level;
				main.g.initGame(); // Re-initialize game for new level layout
				break;
            // Optional: Arrow keys to navigate buttons? (More complex)
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(!isDead && pressed == -1) { // Only register press if nothing else is pressed
			int x = e.getX();
			int y = e.getY();

			// Button 0: "Start"
			if(x >= 300 && x <= 500 && y >= 350 && y <= 450) {
				pressed = 0;
				return; // Found press, exit
			}
			// Difficulty Buttons:
			for(int i = 0; i < Assets.difficulty.length; i++) {
				if(x >= 0 && x <= 100 && y >= 215+i*35 && y < 250+i*35) {
					pressed = i + 1; // Button index 1, 2, ...
					return; // Found press, exit
				}
			}
			// Level Buttons:
			for(int i = 0; i < Assets.levels.length; i++) {
				if(x >= 700 && x <= 800 && y >= 215+i*35 && y < 250+i*35) {
					pressed = i + 1 + Assets.difficulty.length; // Continue indexing
					return; // Found press, exit
				}
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(!isDead && pressed != -1) { // Only process release if a button was pressed
			int x = e.getX();
			int y = e.getY();
			int releasedButton = pressed; // Store which button was originally pressed
			pressed = -1; // Reset pressed state immediately

			// Check if the release happened *inside* the bounds of the originally pressed button
			switch(releasedButton) {
				case 0: // Button 0: "Start"
					if(x >= 300 && x <= 500 && y >= 350 && y <= 450) {
						main.changeOverlay(null); // Start game
					}
					break;
				// --- Difficulty Buttons ---
				case 1: // Button 1: "normal"
					if(x >= 0 && x <= 100 && y >= 215 && y <= 250) {
						difficulty = 0;
						main.g.difficulty = difficulty;
					}
					break;
				case 2: // Button 2: "hard"
					if(x >= 0 && x <= 100 && y >= 250 && y <= 285) { // Bounds were slightly off
						difficulty = 1;
						main.g.difficulty = difficulty;
					}
					break;
				// --- Level Buttons ---
				case 3: // Button 3: "none"
					if(x >= 700 && x <= 800 && y >= 215 && y <= 250) {
						if (level != 0) { // Only reset if changing
							level = 0;
							main.g.level = level;
							main.g.initGame();
						}
					}
					break;
				case 4: // Button 4: "level 1"
					if(x >= 700 && x <= 800 && y >= 250 && y <= 285) { // Bounds corrected
						if (level != 1) {
							level = 1;
							main.g.level = level;
							main.g.initGame();
						}
					}
					break;
				case 5: // Button 5: "level 2"
					if(x >= 700 && x <= 800 && y >= 285 && y <= 320) { // Bounds corrected
						if (level != 2) {
							level = 2;
							main.g.level = level;
							main.g.initGame();
						}
					}
					break;
				case 6: // Button 6: "big cube"
					if(x >= 700 && x <= 800 && y >= 320 && y <= 355) { // Bounds corrected
						if (level != 3) {
							level = 3;
							main.g.level = level;
							main.g.initGame();
						}
					}
					break;
			}
			// No need to set main.g.difficulty again here, it's done when selection changes
		}
        // Ensure pressed is reset even if release is outside
         if (pressed != -1 && !isDead) {
            pressed = -1;
         }
	}

	@Override
	public void paint(Graphics2D g2d) {
		// --- Menu Text ---
		g2d.setColor(Assets.textColor); // Use black text
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 14)); // Slightly larger/clearer font
		g2d.drawString("Press Space or Enter to Start.", 5, 20);
		g2d.drawString("Use Left/Right Arrows (← →) to turn snake.", 5, 40);
        g2d.drawString("Use W/A/S/D to rotate cube.", 5, 60);
		g2d.drawString("Use G / L to cycle Difficulty / Level.", 5, 80);

		// --- Cube Rotation ---
		long currentTime = System.currentTimeMillis();
		if(currentTime - lastT > 1000) { // Prevent large jumps if paused
			lastT = currentTime - 16; // Assume ~60fps if paused long
		}
        // Rotate around Y (Yaw) with ax, X (Pitch) with ay for more intuitive control
		Assets.ax += (currentTime - lastT) / 2000.0; // Spin slower
		lastT = currentTime;

		// --- Draw Content based on State ---
		if(isDead) {
			// --- Death Screen ---
			g2d.setColor(Assets.deathColor); // Red text
			g2d.setFont(new Font("SansSerif", Font.BOLD, 100)); // Bolder font
            // Center text better
            String text1 = "Game";
            String text2 = "Over!";
            int w1 = g2d.getFontMetrics().stringWidth(text1);
            int w2 = g2d.getFontMetrics().stringWidth(text2);
            g2d.drawString(text1, (800 - w1) / 2, 350);
            g2d.drawString(text2, (800 - w2) / 2, 450);

            g2d.setFont(new Font("SansSerif", Font.PLAIN, 20));
            String restartMsg = "Press any key to restart";
            int w3 = g2d.getFontMetrics().stringWidth(restartMsg);
            g2d.drawString(restartMsg, (800-w3)/2, 500);

		} else {
			// --- Main Menu Screen ---

			// Button 0: "Start"
			Color startButtonColor = (pressed == 0) ? Assets.pressColor : Assets.buttonColor;
			g2d.setColor(startButtonColor);
			g2d.fillRect(300, 350, 200, 100);
			g2d.setColor(Assets.lineColor); // Use gray lines for border
			g2d.drawRect(300, 350, 200, 100);
			g2d.setColor(Assets.textColor); // Black text
			g2d.setFont(new Font("SansSerif", Font.BOLD, 50));
            String startText = "Start";
            int startW = g2d.getFontMetrics().stringWidth(startText);
            g2d.drawString(startText, 300 + (200 - startW) / 2, 415);

			// --- Difficulty Selection ---
			g2d.setColor(Assets.textColor);
			g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
			g2d.drawString("Difficulty:", 5, 200);
			g2d.setFont(new Font("SansSerif", Font.PLAIN, 18)); // Font for items

			for(int i = 0; i < Assets.difficulty.length; i++) {
				Color btnColor;
				if(difficulty == i) {
					btnColor = Assets.selectionColor; // Selected = Light Blue
				} else if(i+1 == pressed) {
					btnColor = Assets.pressColor;     // Pressed = Darker Gray
				} else {
					btnColor = Assets.buttonColor;    // Normal = Light Gray
				}
				g2d.setColor(btnColor);
				g2d.fillRect(0, 215+i*35, 100, 30); // Adjusted width slightly
				g2d.setColor(Assets.lineColor); // Gray border
				g2d.drawRect(0, 215+i*35, 100, 30);
				g2d.setColor(Assets.textColor); // Black text
                String diffText = Assets.difficulty[i];
                int diffW = g2d.getFontMetrics().stringWidth(diffText);
				g2d.drawString(diffText, (100 - diffW) / 2, 237+35*i); // Center text
			}

			// --- Level Selection ---
			g2d.setColor(Assets.textColor);
			g2d.setFont(new Font("SansSerif", Font.BOLD, 20));
			g2d.drawString("Level:", 700, 200); // Adjusted position slightly
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 18)); // Font for items

			for(int i = 0; i < Assets.levels.length; i++) {
				Color btnColor;
				int buttonIndex = i + 1 + Assets.difficulty.length;
				if(level == i) {
					btnColor = Assets.selectionColor; // Selected = Light Blue
				} else if(buttonIndex == pressed) {
					btnColor = Assets.pressColor;     // Pressed = Darker Gray
				} else {
					btnColor = Assets.buttonColor;    // Normal = Light Gray
				}
				g2d.setColor(btnColor);
				g2d.fillRect(700, 215+i*35, 100, 30); // Adjusted width/pos slightly
				g2d.setColor(Assets.lineColor); // Gray border
				g2d.drawRect(700, 215+i*35, 100, 30);
				g2d.setColor(Assets.textColor); // Black text
                String lvlText = Assets.levels[i];
                int lvlW = g2d.getFontMetrics().stringWidth(lvlText);
				g2d.drawString(lvlText, 700 + (100 - lvlW) / 2, 237+35*i); // Center text
			}
		}
	}
}