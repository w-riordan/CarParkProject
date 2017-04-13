package application.parking;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

public class Space {
	Polygon space;
	boolean occupied = false;
	int area = 0;
	String name = "";

	public Space(int[] xpoints, int[] ypoints, String name) {
		space = new Polygon(xpoints, ypoints, 4);
		this.name = name;
		calculateArea();
	}

	public void setOccupied(boolean occupied) {
		this.occupied = occupied;
	}

	private void calculateArea() {
		int minX =getBounds().x;
		int maxX =getBounds().x+ getBounds().width;
		int minY =getBounds().y;
		int maxY =getBounds().y+ getBounds().height;
		
		for (int y = minY; y < maxY; y++){
			for (int x = minX; x < maxX; x++){
				if ( isPointInSpace(x, y)){
					area++;
				}
			}
		}
		System.out.println("area is : " + area);
	}

	public boolean isPointInSpace(int x,int y){
		return space.contains(x, y);
	}

	public Rectangle getBounds() {
		return space.getBounds();
	}
	
	public Polygon getPolygon() {
		return space;
	}

	public boolean isOccupied() {
		return occupied;
	}
	
	public String getName() {
		return name;
	}
}
