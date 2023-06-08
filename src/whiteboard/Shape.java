package whiteboard;

/***
 * Name: Jie Yang
 * Student ID: 1290106
 * E-mail: jieyang3@student.unimelb.edu.au
 */

import java.awt.Color;
import java.awt.Graphics;

/**
 * Parent class for all shapes
 */
public abstract class Shape {
	
	protected int x1, y1, x2, y2;
	protected String geo;
	protected Color color;
	
	/**
	 * Initialize a shape
	 * @param x1 start x coordinate
	 * @param y1 start y coordinate
	 * @param x2 end x coordinate
	 * @param y2 end y coordinate
	 * @param geo the type of shape
	 * @param color the color of shape
	 */
	public Shape(int x1, int y1, int x2, int y2, String geo, Color color) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.geo = geo;
		this.color = color;
	}
	
	public abstract void draw(Graphics brush);

	// get start x coordinate
	public int getX1() {
		return this.x1;
	}
	
	// get start y coordinate
	public int getY1() {
		return this.y1;
	}
	
	// get end x coordinate
	public int getX2() {
		return this.x2;
	}
	
	// get end y coordinate
	public int getY2() {
		return this.y2;
	}
	
	// get shape type
	public String getGeo() {
		return this.geo;
	}
	
	// get color R value
	public int getR() {
		return this.color.getRed();
	}
	
	// get color G value
	public int getG() {
		return this.color.getGreen();
	}
	
	// get color B value
	public int getB() {
		return this.color.getBlue();
	}
}
