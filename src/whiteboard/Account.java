package whiteboard;

/***
 * Name: Jie Yang
 * Student ID: 1290106
 * E-mail: jieyang3@student.unimelb.edu.au
 */


import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.json.simple.JSONObject;

/***
 * Represent individual visitor shown on people panel.
 */
public class Account extends JPanel {

	private static final long serialVersionUID = 1L;
	private DataOutputStream output;
	private boolean isEmpty = true;
	private int id;
	private JLabel name;
	private JButton kick;
	private JButton status;
	
	/**
	 * Initialize the entry and hide
	 * @param id index of this object in shared array
	 * @param o output stream used for communication
	 */
	public void init(int id, DataOutputStream o) {
		this.setLayout(null);
		this.name = new JLabel();
		this.kick = new JButton("Kick");
		this.status = new JButton();
		this.status.setEnabled(false);
		
		this.status.setBounds(0, 0, 30, 25);
		this.name.setBounds(35, 0, 90, 25);
		this.kick.setBounds(120, 0, 60, 25);
		
		this.status.setBackground(Color.WHITE);
		this.status.setBorderPainted(false);
		this.kick.setBackground(Color.LIGHT_GRAY);
		
		this.output = o;
		
		// the event only send request but not unbind the entry immediately
		this.kick.addMouseListener(new MouseAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					JSONObject command = new JSONObject();
					command.put("type", "Kick");
					command.put("id", ""+getID());
					output.writeUTF(command.toJSONString());
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		});
		
		this.hideComponents();
		
		this.add(this.name);
		this.add(this.kick);
		this.add(this.status);
		
		this.id = id;
	}
	
	/**
	 * check if this entry is bound with a visitor
	 * @return true if bound otherwise false
	 */
	public boolean isAvailable() {
		return this.isEmpty;
	}
	
	/**
	 * bind this entry with ceratin visitor
	 * @param username the name of visitor
	 */
	public void bind(String username) {
		this.isEmpty = false;
		this.name.setText(username);
		this.showComponents();
	}
	
	/**
	 * unbind this entry and hide it
	 */
	public void unbind() {
		this.deactivate();
		this.hideComponents();
		this.isEmpty = true;
	}
	
	/**
	 * activate the status indicator block
	 */
	public void activate() {
		this.status.setBackground(Color.GREEN);
		this.repaint();
	}
	
	/**
	 * deactivate the status indicator block
	 */
	public void deactivate() {
		this.status.setBackground(Color.WHITE);
		this.repaint();
	}
	
	/**
	 * disable the kick function for client
	 */
	public void disableKick() {
		this.remove(kick);
	}
	
	/**
	 * show status block, name label and kick button(if there is)
	 */
	private void showComponents() {
		this.status.setVisible(true);
		this.name.setVisible(true);
		this.kick.setVisible(true);
	}
	
	/**
	 * hide status block, name label and kick button(if there is)
	 */
	private void hideComponents() {
		this.status.setVisible(false);
		this.name.setVisible(false);
		this.kick.setVisible(false);
	}
	
	/**
	 * get the local id of this entry (same as visiotr id)
	 * @return the local id of this entry
	 */
	public int getID() {
		return this.id;
	}
	
	/**
	 * get the username of visitor bound with this entry
	 * @return username
	 */
	public String getName() {
		return this.name.getText();
	}

}
