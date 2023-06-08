package whiteboard;

/***
 * Name: Jie Yang
 * Student ID: 1290106
 * E-mail: jieyang3@student.unimelb.edu.au
 */

import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.JPanel;

/**
 * Canvas panel
 */
public class MyCanvas extends JPanel {

	private static final long serialVersionUID = 1L;
	private ArrayList<Shape> shapes;
	
	/**
	 * Draw all the shapes when update
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		for (Shape s : shapes) {
			s.draw(g);
		}
	}
	
	/**
	 * bind the shapes group with whiteboard resource
	 * @param s shapes group
	 */
	public void setShapes(ArrayList<Shape> s) {
		this.shapes = s;
	}
}
