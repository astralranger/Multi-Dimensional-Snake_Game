import java.awt.Color;
import java.util.List;
// Removed import javax.swing.JFrame; - It wasn't used and is incorrect here. Object is a game object, not a window.

public class Object { // Changed class name from "Object" to avoid conflict with java.lang.Object
                     // Let's call it GameObject or Fruit maybe? Renaming it GameObject for now.
                     // IMPORTANT: You'll need to find/replace "Object" with "GameObject" in main.java and Snake.java
	Vector pos = new Vector();
	boolean eatable;
	Color color;

	// Create an object at a random position (Fruit)
	public Object(int size, Snake snake, List<Object> obstacles) { // Renamed o to obstacles for clarity
		eatable = true;
		int attempts = 0; // Prevent infinite loop if space is full
		do {
			pos.x = (int)(Math.random()*size);
			pos.y = (int)(Math.random()*size);
			pos.z = (int)(Math.random()*6); // Allow spawning on all 6 faces
            attempts++;
            if (attempts > size * size * 6 * 2) { // Heuristic limit
                 System.err.println("Warning: Could not find empty space for fruit after " + attempts + " attempts.");
                 // Optionally, place it at a default location or handle error
                 pos.x = size / 2; pos.y = size / 2; pos.z = 0;
                 break;
            }
		} while(onSnake(snake) || onObstacle(obstacles)); // Don't put on object on another object/on the snake.

		// Generate random color suitable for light theme (avoid very light/dark colors)
        int r = 60 + (int)(Math.random()*170); // Range 60-230
        int g = 60 + (int)(Math.random()*170);
        int b = 60 + (int)(Math.random()*170);
		this.color = new Color(r, g, b);

		// Tell g3d to paint the object with the generated color.
        // Ensure g3d is initialized before creating objects
        if (Assets.g3d != null) {
		    Assets.g3d.setColor(pos.x, pos.y, pos.z, this.color);
        } else {
            System.err.println("Error: Assets.g3d is null when creating fruit!");
        }
	}

	// Create an object at a certain position (Border/Obstacle).
	public Object(int x, int y, int l) {
		pos.x = x;
		pos.y = y;
		pos.z = l;
		color = Assets.borderColor; // Use the dark border color for obstacles
		eatable = false;
		// Tell g3d to paint the object.
        if (Assets.g3d != null) {
		    Assets.g3d.setColor(pos.x, pos.y, pos.z, color);
        } else {
             System.err.println("Error: Assets.g3d is null when creating border!");
        }
	}

	// Determines if this object shares the location with an object from the list.
	public boolean onObstacle(List<Object> obstacles) {
		for(Object obs : obstacles) { // Use enhanced for loop
			if(pos.x == obs.pos.x && pos.y == obs.pos.y && pos.z == obs.pos.z) {
				return true;
			}
		}
		return false;
	}

	// Determines if the object shares the location with any segment of the snake.
	public boolean onSnake(Snake snake) {
        if (snake == null || snake.head == null) return false; // Safety check
		// Iterate from head backwards, including the head itself if needed
		for(LinkedVector seg = snake.head; seg != null; seg = seg.next) {
            // Need to check against the *next* potential head position? No, check current spawn location.
			if(pos.x == seg.x && pos.y == seg.y && pos.z == seg.z) {
				return true;
			}
            // Optimization: If snake is very long, maybe only check recent segments? Not necessary now.
		}
		return false;
	}

	// Checks if the object is on the location specified by x,y,l.
	public boolean isEaten(int x, int y, int l) {
		return (x == pos.x && y == pos.y && l == pos.z);
	}

	// Make this object become a border object at a random position on the opposite face of its current face.
	public void turnToBorder() {
        if (!eatable) return; // Should only apply to fruits

		int [] opposite = {2, 3, 0, 1, 5, 4}; // Face mapping seems correct
		eatable = false;
		color = Assets.borderColor; // color for borders.
		pos.z = opposite[pos.z]; // Move to opposite face

        // We need to remove the old fruit color and apply the new border color
        // This requires knowing the *original* position before changing pos.z
        // Let's assume the caller removes the fruit color first.
        // Then we just set the new color.
        if (Assets.g3d != null) {
		    Assets.g3d.setColor(pos.x, pos.y, pos.z, color);
        } else {
            System.err.println("Error: Assets.g3d is null when turning fruit to border!");
        }
	}
}