import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints; // Keep anti-aliasing if added previously
import java.awt.BasicStroke;    // Keep stroke if added previously
import java.awt.Paint;          // Keep paint if added previously

// This class is responsible for 3d-projection thus allowing the project to only depend on the java standard library.
public class Graphics3D {
	private double ax, ay; // Some representative for the angle, to compare to, but not the real angle, which isn't stored anywhere.
	private double bz = 1000; // "Distance between center of screen and eyes of the player in pixels." 1000 seems realistic to me.

	// Coordinates of the center of the cube in pixel. (0, 0, 0) is the center of YOUR screen.
	private int x = 0;
	private int y = 0;
	private int z = 1500;
	// Corners of the cube
	private double [][] points = {
	//	{x, y, z, xProjection, yProjection},
		{-300, -300, -300, 0, 0},
		{-300, -300, 300, 0, 0},
		{-300, 300, -300, 0, 0},
		{-300, 300, 300, 0, 0},
		{300, -300, -300, 0, 0},
		{300, -300, 300, 0, 0},
		{300, 300, -300, 0, 0},
		{300, 300, 300, 0, 0},
	};
	// Index of the corners of every one of the 6 areas of the cube.
	private int [][] a = {
		{0, 2, 4, 6},
		{4, 6, 5, 7},
		{5, 7, 1, 3},
		{1, 3, 0, 2},
		{4, 5, 0, 1},
		{2, 3, 6, 7},
	};
	private Color [][][] texture; // Stores a color value for each tile on the surface of the cube.
	private int [] highest = new int[3];
	int size;

	// Set each tile to the background color.
	public Graphics3D(int size) {
		this.size = size;
		texture = new Color[6][size][size];
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < size; j++) {
				for(int k = 0; k < size; k++) {
					texture[i][j][k] = Assets.bgColor; // Use the new background color
				}
			}
		}
	}

	// Rotate the cube to a certain angle representative. Uses only the deltas.
	void reload() {
		update(Assets.ax-this.ax, Assets.ay-this.ay);
		this.ax = Assets.ax;
		this.ay = Assets.ay;
	}

	// Rotate the cube and do all the projection work.
	private void update(double ax, double ay) {
		// Rotate the corners.
		rotateX(ax);
		rotateY(ay);

		// Save the projection for each corner.
		for(int i = 0; i < 8; i++) {
			double x2 = points[i][0]+x;
			double y2 = points[i][1]+y;
			double z2 = points[i][2]+z;
			points[i][3] = get3DX(x2, z2);
			points[i][4] = get3DY(y2, z2);
		}

		// Find the closest point to the screen.
		int closest = 0;
		for(int i = 1; i < 8; i++) {
			if(points[i][2] < points[closest][2])
				closest = i;
		}
		// Find all three areas that that touch the closest point and store their index.
		int k = 0;
		int areas[] = new int[3];
		for(int i = 0; i < 6 && k < 3; i++) {
			for(int j = 0; j < 4; j++) {
				if(a[i][j] == closest) {
					areas[k] = i;
					k++;
					break;
				}
			}
		}
		// Store the average z-distance between the area and the middle-point of the cube. Used to decide which areas to paint and in which order.
		// In a cube this value is r*cos(α) where α is the angle between z-axis and the normal through the area and r is here half the side length.
		double [] val = new double[3];
		for(int i = 0; i < 3; i++) {
			val[i] = 0;
			for(int j = 0; j < 4; j++)
				val[i] += points[a[areas[i]][j]][2];
            val[i] /= 4.0; // Store average Z for potential shading later
		}
		// Sort the areas by their distance to the screen using SelectionSort:
		highest[0] = 0;
		highest[1] = 1;
		highest[2] = 2;
		for(int i = 0; i < 3; i++) {
			for(int j = i+1; j < 3; j++) {
                // Compare the average Z values directly
				if(val[highest[j]] < val[highest[i]]) {
					int local = highest[j];
					highest[j] = highest[i];
					highest[i] = local;
				}
			}
		}
		// put the corresponding area index into the array.
		for(int i = 0; i < 3; i++)
			highest[i] = areas[highest[i]];
	}

	// From now on paint a new color at that tile until changed..
	public void setColor(int x, int y, int l, Color c) {
        // Added bounds check to prevent crashes if snake logic goes wrong
        if (l < 0 || l >= 6 || x < 0 || x >= size || y < 0 || y >= size) {
             System.err.println("setColor out of bounds: l=" + l + ", x=" + x + ", y=" + y);
             return;
        }
		if(texture[l][x][y] != Assets.borderColor) { // Don't change the color, if there is a border tile(Used to make the border not disappear after the snake hit onto it.).
			texture[l][x][y] = c;
		}
	}

	// Paint the cube and its tiles based on the colors stored in texture.
	public void drawCube(Graphics2D g) {
        // --- Optional: Anti-aliasing ---
        java.lang.Object originalAntialiasHint = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        java.lang.Object originalTextAntialiasHint = g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // -------------------------------

		// Only paint three faces of the cube.
		g.translate(400, 400); // Translate to the center of the frame.
		for(int m = 2; m >= 0; m--) {
			int k = highest[m];
			// Store the corners of the current face to paint.
			int [] x = {(int)points[a[k][1]][3], (int)points[a[k][0]][3], (int)points[a[k][2]][3], (int)points[a[k][3]][3]};
			int [] y = {(int)points[a[k][1]][4], (int)points[a[k][0]][4], (int)points[a[k][2]][4], (int)points[a[k][3]][4]};

			// Draw one big polygon in the background color instead of drawing multiple smaller ones:
			g.setColor(Assets.bgColor); // Use the updated background color
			g.fillPolygon(x, y, 4);

			// Divide the polygon created by the array directly above into an N*N field of tiles:
			double nx = (x[1]-x[0])/(double)size; // x-size of a tile at one side
			double ny = (y[1]-y[0])/(double)size; // y-size of a tile at one side
			double fx = nx+(x[3]-x[2])/(double)size; // x-size of a tile at the other side
			double fy = ny+(y[3]-y[2])/(double)size; // y-size of a tile at the other side

			for(int i = 0; i < size; i++) {
				// Use a linear transition between n and f to determine respective size and position:
				double nx1 = x[0]+i*nx;
				double nx2 = x[0]+(i+1)*nx;
				double ny1 = y[0]+i*ny;
				double ny2 = y[0]+(i+1)*ny;
				// Recalculated intermediate points for tile corners
                // Vector from corner 0 to 3 (scaled by i/size)
                double dx_i = (x[3]-x[0]) * i / (double)size;
                double dy_i = (y[3]-y[0]) * i / (double)size;
                // Vector from corner 0 to 3 (scaled by (i+1)/size)
                double dx_i1 = (x[3]-x[0]) * (i+1) / (double)size;
                double dy_i1 = (y[3]-y[0]) * (i+1) / (double)size;

				// Finally draw the polygons of each row or column(depending on the rotation of the cube).
				for(int j = 0; j < size; j++) {
                    // Recalculate tile corners more directly using interpolation
                    // Top-left corner of tile (i,j)
                    double p0x = nx1 + (x[2]-x[1]) * j / (double)size;
                    double p0y = ny1 + (y[2]-y[1]) * j / (double)size;
                    // Top-right corner of tile (i,j)
                    double p1x = nx1 + (x[2]-x[1]) * (j+1) / (double)size;
                    double p1y = ny1 + (y[2]-y[1]) * (j+1) / (double)size;
                    // Bottom-right corner of tile (i,j)
                    double p2x = nx2 + (x[3]-x[0]) * (j+1) / (double)size; // Error in original? Should relate to x[3]/x[2]
                    double p2y = ny2 + (y[3]-y[0]) * (j+1) / (double)size; // Error in original? Should relate to y[3]/y[2]

                    // Let's try linear interpolation between the 4 main corners (x[0]..x[3])
                    // Interpolate along top edge (x[0] to x[1]) and bottom edge (x[3] to x[2])
                    double top_x1 = x[0] + (x[1]-x[0])*j/(double)size;
                    double top_y1 = y[0] + (y[1]-y[0])*j/(double)size;
                    double top_x2 = x[0] + (x[1]-x[0])*(j+1)/(double)size;
                    double top_y2 = y[0] + (y[1]-y[0])*(j+1)/(double)size;
                    double bot_x1 = x[3] + (x[2]-x[3])*j/(double)size;
                    double bot_y1 = y[3] + (y[2]-y[3])*j/(double)size;
                    double bot_x2 = x[3] + (x[2]-x[3])*(j+1)/(double)size;
                    double bot_y2 = y[3] + (y[2]-y[3])*(j+1)/(double)size;

                    // Interpolate vertically between the interpolated edge points
                    int tile_x0 = (int)(top_x1 + (bot_x1 - top_x1) * i / (double)size);
                    int tile_y0 = (int)(top_y1 + (bot_y1 - top_y1) * i / (double)size);
                    int tile_x1 = (int)(top_x2 + (bot_x2 - top_x2) * i / (double)size);
                    int tile_y1 = (int)(top_y2 + (bot_y2 - top_y2) * i / (double)size);
                    int tile_x2 = (int)(top_x2 + (bot_x2 - top_x2) * (i+1) / (double)size);
                    int tile_y2 = (int)(top_y2 + (bot_y2 - top_y2) * (i+1) / (double)size);
                    int tile_x3 = (int)(top_x1 + (bot_x1 - top_x1) * (i+1) / (double)size);
                    int tile_y3 = (int)(top_y1 + (bot_y1 - top_y1) * (i+1) / (double)size);

					if(texture[k][i][j] != Assets.bgColor) { // Only draw the tiles if they don't have the background color.
                        int [] xx = {tile_x0, tile_x1, tile_x2, tile_x3};
						int [] yy = {tile_y0, tile_y1, tile_y2, tile_y3};
						g.setColor(texture[k][i][j]);
						g.fillPolygon(xx, yy, 4);
					}
				}
				// Draw the horizontal grid lines
				g.setColor(Assets.lineColor); // Use updated line color
                int h_x1 = (int)(x[0] + (x[3]-x[0]) * i / (double)size);
                int h_y1 = (int)(y[0] + (y[3]-y[0]) * i / (double)size);
                int h_x2 = (int)(x[1] + (x[2]-x[1]) * i / (double)size);
                int h_y2 = (int)(y[1] + (y[2]-y[1]) * i / (double)size);
				g.drawLine(h_x1, h_y1, h_x2, h_y2);
			}
            // Draw the last horizontal line
            g.setColor(Assets.lineColor);
            g.drawLine(x[3], y[3], x[2], y[2]);

			// Draw the vertical grid lines
			for(int i = 0; i <= size; i++) {
                int v_x1 = (int)(x[0] + (x[1]-x[0]) * i / (double)size);
                int v_y1 = (int)(y[0] + (y[1]-y[0]) * i / (double)size);
                int v_x2 = (int)(x[3] + (x[2]-x[3]) * i / (double)size);
                int v_y2 = (int)(y[3] + (y[2]-y[3]) * i / (double)size);
				g.drawLine(v_x1, v_y1, v_x2, v_y2);
			}
		}
		g.translate(-400, -400);

        // --- Optional: Restore hints ---
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, originalAntialiasHint);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, originalTextAntialiasHint);
        // -----------------------------
	}

	// project the x-coordinate.
	private double get3DX(double x, double z) {
		// Using atan for projection might be more stable than asin/tan
        // return Math.atan(x / z) * bz; // Simpler perspective projection formula
        if (z <= 0) return (x > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY); // Avoid division by zero/sqrt of negative
		x = Math.asin(x/Math.sqrt(x*x+z*z));
		x = Math.tan(x)*bz;
		return x;
	}

	// project the y-coordinate.
	private double get3DY(double y, double z) {
        // return Math.atan(y / z) * bz; // Simpler perspective projection formula
         if (z <= 0) return (y > 0 ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY); // Avoid division by zero/sqrt of negative
		y = Math.asin(y/Math.sqrt(y*y+z*z));
		y = Math.tan(y)*bz;
		return y;
	}

	// rotate around the y-axis (Pitch) - Affects X and Z
	private void rotateY(double a) { // Original code rotates around X axis here
		double sin = Math.sin(a);
	    double cos = Math.cos(a);
	    for (int i = 0; i < 8; i++) {
            double x = points[i][0]; // Corrected: Y rotation affects X and Z
	    	double z = points[i][2];
	        points[i][0] = x * cos - z * sin;
            points[i][2] = z * cos + x * sin;
	    }
	}

	// rotate around the x-axis (Yaw) - Affects Y and Z
	private void rotateX(double a) { // Original code rotates around Y axis here
		double sin = Math.sin(a);
	    double cos = Math.cos(a);
	    for (int i = 0; i < 8; i++) {
	    	double y = points[i][1]; // Corrected: X rotation affects Y and Z
	    	double z = points[i][2];
	        points[i][1] = y * cos - z * sin;
            points[i][2] = z * cos + y * sin;
	    }
	}
    // Optional helper for shading (if used)
    private Color brightenOrDarken(Color color, double factor) {
        int r = (int) Math.max(0, Math.min(255, color.getRed() * factor));
        int g = (int) Math.max(0, Math.min(255, color.getGreen() * factor));
        int b = (int) Math.max(0, Math.min(255, color.getBlue() * factor));
        return new Color(r, g, b, color.getAlpha()); // Preserve alpha
    }
}