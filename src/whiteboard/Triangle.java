package whiteboard;

/***
 * Name: Jie Yang
 * Student ID: 1290106
 * E-mail: jieyang3@student.unimelb.edu.au
 */

import java.awt.Color;
import java.awt.Graphics;

/**
 * Triangle shape
 */
public class Triangle extends Shape{

	/**
	 * Initialize a triangle
	 */
	public Triangle(int x1, int y1, int x2, int y2, String geo, Color color) {
		super(x1, y1, x2, y2, geo, color);
	}

	/**
	 * draw a triangle based on arguments
	 * @param the graphic drawer of canvas
	 */
	@Override
	public void draw(Graphics brush) {
		brush.setColor(this.color);
		int x1 = this.x1;
		int y1 = this.y1;
		int x2 = this.x2;
		int y2 = this.y1;
		int x3 = (this.x1 + this.x2) / 2;
		int y3 = this.y2;
		brush.drawLine(x1, y1, x2, y2);
		brush.drawLine(x2, y2, x3, y3);
		brush.drawLine(x3, y3, x1, y1);
	}

}
