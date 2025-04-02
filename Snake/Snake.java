import java.util.List;

public class Snake {
	LinkedVector head = null; // Use similar structure to a deque.
	LinkedVector tail = null;
	int curLength; // Actual length of the snake.
	int length = 3; // Start with a length of 3. Desired length of the snake.
	private int size; // Stores the max coordinate value (size-1)
	private int [][] diruse = { // direction is encoded as number from 0 to 3. This is the 'encoder'.
		// {dx, dy} - Relative to the current face's coordinate system
        { 0, -1}, // 0: Up
		{ 1,  0}, // 1: Right
		{ 0,  1}, // 2: Down
		{-1,  0}  // 3: Left
	};
	private int dir; // Current direction (0-3)

	public Snake(int size, int x, int y, int l) {
		this.size = size-1; // Use max coordinate value (0 to size-1)
		head = tail = new LinkedVector(x, y, l);
        // Ensure head color is set initially
        if (Assets.g3d != null) {
             Assets.g3d.setColor(head.x, head.y, head.z, Assets.snakeColor);
        }
		curLength = 1;
		dir = 0; // Start moving Up
	}

	// Changes the internal direction based on a relative turn (-1=Left, 1=Right).
    // Absolute direction setting might be less error-prone if needed later.
	public void changedir(int turn) { // Renamed dir param to turn
		this.dir += turn;
		// Wrap around using modulo
        this.dir = (this.dir + 4) % 4;
	}

	// Moves the whole snake one block into the direction given by dir.
	public void move() {
		// --- 1. Calculate new head position ---
        int nextX = head.x + diruse[dir][0];
        int nextY = head.y + diruse[dir][1];
        int nextL = head.z;
        int nextDir = dir; // Store potential new direction after face change

        // --- 2. Handle Face Transitions ---
        // Check boundaries and handle transitions between faces
        if(nextL < 4) { // Faces 0, 1, 2, 3 (Side faces)
            if(nextY < 0) { // Moving up off a side face
                switch(nextL) {
                    case 0: nextL = 5; nextX = head.x; nextY = size; nextDir = 0; break; // Top -> Left face
                    case 1: nextL = 5; nextX = size - head.x; nextY = 0; nextDir = 2; break; // Top -> Left face
                    case 2: nextL = 5; nextX = size - head.x; nextY = size; nextDir = 0; break; // Top -> Left face
                    case 3: nextL = 5; nextX = head.x; nextY = 0; nextDir = 2; break; // Top -> Left face
                }
            }
            else if(nextY > size) { // Moving down off a side face
                switch(nextL) {
                    case 0: nextL = 4; nextX = head.x; nextY = 0; nextDir = 2; break; // Bottom -> Right face
                    case 1: nextL = 4; nextX = size - head.x; nextY = size; nextDir = 0; break; // Bottom -> Right face
                    case 2: nextL = 4; nextX = size - head.x; nextY = 0; nextDir = 2; break; // Bottom -> Right face
                    case 3: nextL = 4; nextX = head.x; nextY = size; nextDir = 0; break; // Bottom -> Right face
                }
            }
            else if(nextX > size) { // Moving right off a side face
                nextL = (nextL + 1) % 4; // Move to next side face clockwise
                nextX = 0; // Appear on left edge of next face
                // Direction stays the same (moving right)
            }
            else if(nextX < 0) { // Moving left off a side face
                nextL = (nextL + 3) % 4; // Move to previous side face counterclockwise
                nextX = size; // Appear on right edge of previous face
                // Direction stays the same (moving left)
            }
        }
        else if(nextL == 4) { // Right face
            if(nextX < 0) { // Moving left off right face
                nextL = 3; // Go to last side face
                nextX = size;
                // Direction stays the same
            }
            else if(nextX > size) { // Moving right off right face
                nextL = 1; // Go to second side face
                nextX = 0;
                // Direction stays the same
            }
            else if(nextY < 0) { // Moving up off right face
                nextL = 0; // Go to first side face
                nextY = head.x;
                nextX = size;
                nextDir = 3; // Change direction to left
            }
            else if(nextY > size) { // Moving down off right face
                nextL = 2; // Go to third side face
                nextY = size - head.x;
                nextX = size;
                nextDir = 3; // Change direction to left
            }
        }
        else { // Left face (nextL == 5)
            if(nextX < 0) { // Moving left off left face
                nextL = 1; // Go to second side face
                nextX = size;
                // Direction stays the same
            }
            else if(nextX > size) { // Moving right off left face
                nextL = 3; // Go to last side face
                nextX = 0;
                // Direction stays the same
            }
            else if(nextY < 0) { // Moving up off left face
                nextL = 0; // Go to first side face
                nextY = head.x;
                nextX = 0;
                nextDir = 1; // Change direction to right
            }
            else if(nextY > size) { // Moving down off left face
                nextL = 2; // Go to third side face
                nextY = size - head.x;
                nextX = 0;
                nextDir = 1; // Change direction to right
            }
        }


        // --- 3. Update Snake Body ---
        // Remove the old tail segment visually ONLY IF snake isn't growing
        if (curLength >= length) {
             if (Assets.g3d != null && tail != null) {
                 Assets.g3d.setColor(tail.x, tail.y, tail.z, Assets.bgColor);
             }
             // Move tail pointer
             if (tail != null && tail.previous != null) {
                 tail = tail.previous;
                 tail.unlink(); // Break link from new tail to old tail
             } else {
                 // If tail.previous is null, snake is length 1, tail becomes null
                 tail = null;
             }
        } else {
            curLength++; // Grow snake
        }

		// --- 4. Add new head ---
		LinkedVector newHead = new LinkedVector(nextX, nextY, nextL);
        dir = nextDir; // Update direction *after* calculating position

        // Link new head to old head
        newHead.next = head;
        if (head != null) {
            head.previous = newHead;
        }
        head = newHead; // Update head pointer

        // Update tail pointer if snake was length 0 or 1
        if (tail == null) {
            tail = head;
        }


        // --- 5. Draw new head ---
        if (Assets.g3d != null) {
		    Assets.g3d.setColor(head.x, head.y, head.z, Assets.snakeColor);
        }
	}

	// Determines if the object is eaten. Pass the specific fruit object.
	public boolean eat(Object fruit) { // Use specific class if renamed (e.g., GameObject fruit)
        if (fruit == null || !fruit.eatable) return false; // Can only eat eatable fruits

		if(fruit.isEaten(head.x, head.y, head.z)) {
			length++; // Increase desired length
			return true;
		}
		return false;
	}

	// Determines if the snake bites its own tail (or body).
	public boolean eatSelf() {
		if(curLength < 4) { // Cannot bite self if length 3 or less
             return false;
        }
        // Start checking from the segment *after* the head
		if (head != null && head.next != null) {
            for(LinkedVector seg = head.next; seg != null; seg = seg.next) {
				if(head.x == seg.x && head.y == seg.y && head.z == seg.z) {
					return true; // Collision detected
				}
			}
		}
		return false;
	}

	// Determines if the snake hits an obstacle (border object).
	public boolean eatBorder(List<Object> obstacles) { // Use specific class if renamed
		for(Object obs : obstacles) {
            if (!obs.eatable) { // Only check non-eatable obstacles
			    if(obs.isEaten(head.x, head.y, head.z)) {
				    return true; // Collision detected
			    }
            }
		}
		return false;
	}
}