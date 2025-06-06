// Vector used to store the 3 coordinates of every ingame object.
// z determines on which of the 6 faces the object is (0-5).

public class Vector {
	int x, y, z;
	public Vector() {
		x = y = z = 0;
	}
	public Vector(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
}