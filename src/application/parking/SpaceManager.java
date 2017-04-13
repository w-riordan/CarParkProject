package application.parking;

import java.util.ArrayList;

public class SpaceManager {
	ArrayList<Space> spaces;
	Space selectedSpace=null;
	
	int[] x = new int[4];
	int[] y = new int[4];
	
	int nameCounter=1;
	int pointCounter=0;
	
	public SpaceManager() {
		spaces = new ArrayList<Space>();
	}
	
	public ArrayList<Space> getSpaces() {
		return spaces;
	}
	
	public void deleteSpace(Space s){
		spaces.remove(s);
	}
	
	public void addPoint(int x, int y){
		this.x[pointCounter] = x;
		this.y[pointCounter] = y;
		pointCounter++;
		System.err.println("Added point " +x + ","+y);
		if (pointCounter>3){
			spaces.add(new Space(this.x, this.y, "Space"+(nameCounter++)));
			pointCounter = 0;
			System.err.println("Added Space");
		}
	}
	
	
}
