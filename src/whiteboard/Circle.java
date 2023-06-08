package whiteboard;

/***
 * Name: Jie Yang
 * Student ID: 1290106
 * E-mail: jieyang3@student.unimelb.edu.au
 */

import java.awt.Color;
import java.awt.Graphics;

/**
 * Circle shape
 */
public class Circle extends Shape {

	/**
	 * Initialize a circle
	 */
	public Circle(int x1, int y1, int x2, int y2, String geo, Color color) {
		super(x1, y1, x2, y2, geo, color);
	}

	/**
	 * draw a circle based on arguments
	 * @param the graphic drawer of canvas
	 */
	@Override
	public void draw(Graphics brush) {
		brush.setColor(this.color);
		int x = Math.min(this.x1, this.x2);
		int y = Math.min(this.y1, this.y2);
		int width = Math.abs(this.x2-this.x1);
		int height = Math.abs(this.y2-this.y1);
		int diameter = Math.max(width, height);
		brush.drawOval(x, y, diameter, diameter);
	}
}
