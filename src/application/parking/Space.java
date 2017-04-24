package application.parking;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

/**
 * This class represents a parking space
 */
public class Space {
	private Polygon space;
	private boolean occupied = false;
	private boolean occupiedCheck = false;
	private int occupiedCounter = 0;
	private int area = 0;
	private String name = "";

	/**
	 * Contructs a new space
	 * 
	 * @param xpoints
	 *            - the x points of the space
	 * @param ypoints
	 *            - the y co-ordinates of the space
	 * @param name
	 *            - the name of the space
	 */
	public Space(int[] xpoints, int[] ypoints, String name) {
		space = new Polygon(xpoints, ypoints, 4);
		this.name = name;
		calculateArea();
	}

	/**
	 * sets the space to occupied or not
	 * 
	 * @param occupied
	 *            - whether or not its occupied
	 */
	public void setOccupied(boolean occupied) {
		if (occupied == occupiedCheck) {
			occupiedCounter++;
			if (occupiedCounter >= MonitorSettings.occupiedTime) {
				this.occupied = occupied;
			}
		} else {
			occupiedCounter = 0;
			occupiedCheck = occupied;
		}
	}

	/**
	 * calculates the area of the space
	 */
	private void calculateArea() {
		int minX = getBounds().x;
		int maxX = getBounds().x + getBounds().width;
		int minY = getBounds().y;
		int maxY = getBounds().y + getBounds().height;

		for (int y = minY; y < maxY; y++) {
			for (int x = minX; x < maxX; x++) {
				if (isPointInSpace(x, y)) {
					area++;
				}
			}
		}
	}

	/**
	 * Returns whether or not a point is in this space
	 * 
	 * @param x
	 *            - x co-ordinate of point
	 * @param y
	 *            - y co-ordinate of point
	 * @return boolean
	 */
	public boolean isPointInSpace(int x, int y) {
		return space.contains(x, y);
	}

	/**
	 * Returns the bounds of the space as a Rectangle
	 * 
	 * @return Rectangle
	 */
	public Rectangle getBounds() {
		return space.getBounds();
	}

	/**
	 * Returns the polygon of the space
	 * 
	 * @return Polygon
	 */
	public Polygon getPolygon() {
		return space;
	}

	/**
	 * returns whether the space is currently occupied
	 * 
	 * @return boolean
	 */
	public boolean isOccupied() {
		return occupied;
	}

	/**
	 * Returns the name of the space
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * returns the area of the space as an int
	 * 
	 * @return int
	 */
	public int getArea() {
		return area;
	}
}
