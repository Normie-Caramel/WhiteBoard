package whiteboard;

/***
 * Name: Jie Yang
 * Student ID: 1290106
 * E-mail: jieyang3@student.unimelb.edu.au
 */

import java.awt.Color;
import java.awt.Graphics;

/**
 * Line shape
 */
public class Line extends Shape {
	
	/**
	 * Initialize a line
	 */
	public Line(int x1, int y1, int x2, int y2, String geo, Color color) {
		super(x1, y1, x2, y2, geo, color);
	}

	/**
	 * draw a line based on arguments
	 * @param the graphic drawer of canvas
	 */
	@Override
	public void draw(Graphics brush) {
		brush.setColor(this.color);
		brush.drawLine(this.x1, this.y1, this.x2, this.y2);
	}

}
