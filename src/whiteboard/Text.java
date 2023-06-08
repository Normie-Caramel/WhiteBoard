package whiteboard;

/***
 * Name: Jie Yang
 * Student ID: 1290106
 * E-mail: jieyang3@student.unimelb.edu.au
 */

import java.awt.Color;
import java.awt.Graphics;

/**
 * Text shape
 */
public class Text extends Shape {
	
	private String content;
	
	/**
	 * Initialize a text with additional content
	 */
	public Text(int x1, int y1, int x2, int y2, String geo, Color color, String content) {
		super(x1, y1, x2, y2, geo, color);
		this.content = content;
	}

	/**
	 * draw a text based on arguments
	 * @param the graphic drawer of canvas
	 */
	@Override
	public void draw(Graphics brush) {
		brush.setColor(this.color);
		brush.drawString(content, x1, y1);
	}
	
	/**
	 * get the content of text shape
	 * @return content of text shape
	 */
	public String getContent() {
		return this.content;
	}

}
