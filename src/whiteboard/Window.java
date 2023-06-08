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
import javax.swing.JTextArea;

import org.json.simple.JSONObject;

public class Window extends JPanel {

	private static final long serialVersionUID = 1L;
	private boolean isChat;
	private JLabel prompt;
	private JTextArea info;
	private JButton submit;
	private JTextArea chatbox;
	private String username;
	private DataOutputStream output;
	
	/**
	 * Initialize a text line and bind event listener
	 * @return this object
	 */
	public Window init() {
		
		this.setBounds(10, 510, 770, 40);
		this.setLayout(null);
		
		this.isChat = true;
		this.prompt = new JLabel("Chat :");
		this.info = new JTextArea("");
		this.submit = new JButton("submit");
		
		this.prompt.setBounds(10, 10, 60, 30);
		this.info.setBounds(70, 10, 600, 30);
		this.submit.setBounds(680, 10, 90, 30);
		
		// when text line is used for chatting, it will send content to server once submit
		this.submit.addMouseListener(new MouseAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void mouseClicked(MouseEvent e) {
				if(isChatType()) {
					String content = getInfo();
					if(content.trim().length() != 0) {
						// update locally and then send to server
						sendChat(content);
						try {
							JSONObject query = new JSONObject();
							query.put("type", "Chat");
							query.put("username", getUsername());
							query.put("content", content);
							output.writeUTF(query.toJSONString());
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
		
		this.add(this.prompt);
		this.add(this.info);
		this.add(this.submit);
		
		return this;
	}
	
	/**
	 * bind the chat box used for showing the message
	 * @param chatbox chatbox in whiteboard window
	 * @param username the username of local client
	 */
	public void bindChat(JTextArea chatbox, String username) {
		this.chatbox = chatbox;
		this.username = username;
	}
	
	/**
	 * bind the communication output stream
	 * @param output the communication output stream
	 */
	public void bindOutput(DataOutputStream output) {
		this.output = output;
	}
	
	/**
	 * change the function of text line from chatting to entering content of text shape
	 * @param type text shape prompt
	 */
	public void setType(String type) {
		this.isChat = false;
		this.submit.setEnabled(false);
		this.prompt.setText(type + " :");
		this.prompt.setForeground(Color.RED);
	}
	
	/**
	 * change the function of text line from entering content to chatting
	 */
	public void release() {
		this.isChat = true;
		this.submit.setEnabled(true);
		this.prompt.setText("Chat :");
		this.prompt.setForeground(Color.BLACK);
	}
	
	/**
	 * check the current function
	 * @return true if function is chatting false otherwise
	 */
	public boolean isChatType() {
		return this.isChat;	
	}
	
	/**
	 * get the content typed by user and clear the text line.
	 * @return content
	 */
	public String getInfo() {
		String info = this.info.getText();
		this.info.setText("");
		return info;
	}
	
	/**
	 * prompt the content entered by self on local chatbox
	 * @param content content entered by self
	 */
	public void sendChat(String content) {
		this.chatbox.append(this.username + ": " + content + "\n");
	}
	
	/**
	 * get the username of local client
	 * @return the username of local client
	 */
	public String getUsername() {
		return this.username;
	}
}
