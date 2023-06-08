package whiteboard;

/***
 * Name: Jie Yang
 * Student ID: 1290106
 * E-mail: jieyang3@student.unimelb.edu.au
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;

import org.json.simple.JSONObject;

/**
 * A listener listening on canvas and tool board, which is used for painting.
 */
public class Listener implements MouseListener, MouseMotionListener, ActionListener {
	
	private ArrayList<Shape> shapes;
	private Graphics brush;
	private Window prompt;
	private int id;
	private Account[] accounts;
	private Color color = Color.BLACK;
	private String geo = "Line";
	private int x1, y1, x2, y2;
	private DataOutputStream output;
	
	/**
	 * Share the shapes group with whiteboard
	 * @param s shapes group
	 */
	public void setShapes(ArrayList<Shape> s) {
		this.shapes = s;
	}
	
	/**
	 * Bind with the canvas drawer
	 * @param brush canvas drawer
	 */
	public void setBrush(Graphics brush) {
		this.brush = brush;
	}
	
	/**
	 * Bind with communication channel
	 * @param output communication send channel
	 */
	public void setOutput(DataOutputStream output) {
		this.output = output;
	}
	
	/**
	 * Bind with text line that used for geting content of text shape
	 * @param prompt text line
	 */
	public void setPrompt(Window prompt) {
		this.prompt = prompt;
	}
	
	/**
	 * Get self id and accounts maintained used to trigger indicator block
	 * @param id
	 * @param accounts
	 */
	public void setPeople(int id, Account[] accounts) {
		this.id = id;
		this.accounts = accounts;
	}
	
	/**
	 * change brush color and shape
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		try {
			if ("".equals(e.getActionCommand())) {
				JButton bt = (JButton)e.getSource();
				this.color = bt.getBackground();
				this.brush.setColor(this.color);
			} else {
				this.geo = e.getActionCommand();
				
				// when user click text shape, he/she will be deemed active
				if ("Text".equals(this.geo)) {
					this.prompt.setType("Text");
					this.accounts[this.id].activate();
					JSONObject info = new JSONObject();
					info.put("type", "Drawing");
					info.put("id", ""+this.id);
					this.output.writeUTF(info.toJSONString());
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * paint the text on the canvas according to position got
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		this.x1 = e.getX();
		this.y1 = e.getY();
		
		try {
			if(this.geo.equals("Text")) {
				String content = this.prompt.getInfo();
				this.prompt.release();
				Shape newText = new Text(x1, y1, 0, 0, geo, color, content);
				newText.draw(brush);
				shapes.add(newText);
				JSONObject info = encode(newText);
				this.output.writeUTF(info.toJSONString());
				this.accounts[this.id].deactivate();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
	}

	/**
	 * record the start position of shape and trigger the active status
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void mousePressed(MouseEvent e) {
		this.x1 = e.getX();
		this.y1 = e.getY();
		this.accounts[this.id].activate();
		try {
			JSONObject info = new JSONObject();
			info.put("type", "Drawing");
			info.put("id", ""+this.id);
			this.output.writeUTF(info.toJSONString());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * get the end position, draw and save the shape, and deactive the user status.
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		this.x2 = e.getX();
		this.y2 = e.getY();
		try {
			if(this.geo.equals("Line")) {
				// draw line
				Shape newLine = new Line(x1, y1, x2, y2, geo, color);
				newLine.draw(brush);
				shapes.add(newLine);
				JSONObject info = encode(newLine);
				this.output.writeUTF(info.toJSONString());
			} else if(this.geo.equals("Cir.")) {
				// draw circle
				Shape newCircle = new Circle(x1, y1, x2, y2, geo, color);
				newCircle.draw(brush);
				shapes.add(newCircle);
				JSONObject info = encode(newCircle);
				this.output.writeUTF(info.toJSONString());
			} else if(this.geo.equals("Rect")) {
				// draw rectangle
				Shape newRect = new Rectangle(x1, y1, x2, y2, geo, color);
				newRect.draw(brush);
				shapes.add(newRect);
				JSONObject info = encode(newRect);
				this.output.writeUTF(info.toJSONString());
			} else if(this.geo.equals("Tri.")) {
				// draw triangle
				Shape newTriangle = new Triangle(x1, y1, x2, y2, geo, color);
				newTriangle.draw(brush);
				shapes.add(newTriangle);
				JSONObject info = encode(newTriangle);
				this.output.writeUTF(info.toJSONString());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		this.accounts[this.id].deactivate();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * transfrom a shape object into a JSON object
	 * @param s shape object
	 * @return corresponding JSON obejct
	 */
	@SuppressWarnings("unchecked")
	private JSONObject encode(Shape s) {
		JSONObject shape = new JSONObject();
		shape.put("type", "New Shape");
		shape.put("id", ""+this.id);
		shape.put("x1", ""+s.getX1());
		shape.put("y1", ""+s.getY1());
		shape.put("x2", ""+s.getX2());
		shape.put("y2", ""+s.getY2());
		shape.put("geo", s.getGeo());
		shape.put("r", ""+s.getR());
		shape.put("g", ""+s.getG());
		shape.put("b", ""+s.getB());
		if(s.getGeo().equals("Text")) {
			Text t = (Text)s;
			shape.put("content",t.getContent());
		}
		return shape;
	}

}
