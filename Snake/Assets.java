import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;

// Stores the 3D-graphics object and is responsible for file-saving/-loading.
class Assets {
	static Graphics3D g3d;
	static double ax = 0;
	static double ay = 0;

	// --- Light Theme Colors ---
	static Color borderColor = Color.DARK_GRAY;      // Dark border for contrast on light bg
	static Color bgColor = new Color(245, 245, 245); // Very light gray background
	static Color lineColor = Color.GRAY;             // Lines on the cube grid
	static Color snakeColor = Color.BLUE;            // A distinct snake color
	static Color textColor = Color.BLACK;            // Standard black text for readability
	static Color buttonColor = new Color(220, 220, 220); // Light gray button fill
	static Color pressColor = new Color(180, 180, 180);  // Darker gray when pressed
	static Color selectionColor = new Color(173, 216, 230); // Light blue for selected item
	static Color deathColor = Color.RED;             // Standard red for "Game Over"

	// Text for levels/difficulty.
	static String [] difficulty = {"normal", "hard"};
	static String [] levels = {"none", "level 1", "level 2", "big cube"};

	// save/load highscore
	static void save(int [] score) {
		String s = "";
		for(int k = 0; k < difficulty.length*levels.length; k++) {
			s += score[k]+"\n";
		}
		try {
			// Consider saving to a different file or adding a theme setting if you want both
			FileWriter f = new FileWriter("score.snake3D");
			for(int i = 0; i < s.length(); i++) {
				f.write(s.charAt(i));
			}
			f.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	static int [] load() {
		int ret[] = new int[difficulty.length*levels.length];
		try {
			FileReader fr = new FileReader("score.snake3D");
			BufferedReader br = new BufferedReader(fr);
			for(int i = 0; i < levels.length+difficulty.length*levels.length; i++) {
				String score = br.readLine();
				if (score != null) { // Basic check for empty/corrupt file
					ret[i] = Integer.parseInt(score);
				} else {
					ret[i] = 0; // Default to 0 if line is missing
				}
			}
			br.close();
			fr.close();
		} catch (Exception e) {
            // File might not exist on first run, or be corrupted. Initialize scores to 0.
            System.err.println("Could not load scores, initializing to 0. Error: " + e.getMessage());
            for(int i = 0; i < ret.length; i++) {
                ret[i] = 0;
            }
		}
		return ret;
	}
}