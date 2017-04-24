package application.parking;

import java.util.ArrayList;

/**
 * This class is responsible for managing the Spaces.
 */
public class SpaceManager {
	// holds the spaces
	ArrayList<Space> spaces;

	// the x and y points for a new space
	int[] x = new int[4];
	int[] y = new int[4];

	// Counters used for names and points
	int nameCounter = 1;
	int pointCounter = 0;

	/**
	 * Constructs a new space manager
	 */
	public SpaceManager() {
		spaces = new ArrayList<Space>();
	}

	/**
	 * Returns the spaces stored
	 * 
	 * @return - ArrayList<Space>
	 */
	public ArrayList<Space> getSpaces() {
		return spaces;
	}

	/**
	 * Deletes the space passed in
	 * 
	 * @param s
	 *            - the space to delete
	 */
	public void deleteSpace(Space s) {
		spaces.remove(s);
	}

	/**
	 * Adds a new point. When 4 points have been added it uses them to create a
	 * new space.
	 * 
	 * @param x
	 *            - the x co-ordinate of the point
	 * @param y
	 *            - the y co-ordinate of the point
	 */
	public void addPoint(int x, int y) {
		this.x[pointCounter] = x;
		this.y[pointCounter] = y;
		pointCounter++;
		if (pointCounter > 3) {
			spaces.add(new Space(this.x, this.y, "Space" + (nameCounter++)));
			pointCounter = 0;
		}
	}

}
