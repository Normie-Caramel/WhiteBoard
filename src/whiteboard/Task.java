package whiteboard;

/***
 * Name: Jie Yang
 * Student ID: 1290106
 * E-mail: jieyang3@student.unimelb.edu.au
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Task extends Thread {
	
	private static JSONArray shapes;
	private static Task[] peers;
	private boolean isReady = false;
	private int id;
	private String username;
	private Socket socket;
	private DataInputStream input;
	private DataOutputStream output;
	private JSONParser parser = new JSONParser();
	
	/**
	 * Initialize a task thread
	 * @param id the index of this task in shared array
	 * @param socket the socket peered with a client
	 */
	public Task(int id, Socket socket) throws IOException {
		super();
		this.id = id;
		this.socket = socket;
		this.input = new DataInputStream(socket.getInputStream());
		this.output = new DataOutputStream(socket.getOutputStream());
	}
	
	/**
	 * Initialize a slience task thread to make alive check work
	 * @param id the index of this task in shared array
	 */
	public Task(int id) {
		super();
		this.id = id;
	}

	/**
	 * start listening on the socket and handle the command according to its type
	 */
	@Override
	public void run() {
		while(true) {
			try {
				JSONObject command = (JSONObject)this.parser.parse(input.readUTF());
				String type = (String)command.get("type");
				if (type.equals("New Shape")) {
					newShape(command);
				} else if (type.equals("Chat")) {
					broadcast(command, this.id);
				} else if (type.equals("Quit")) {
					quit();
					break;
				} else if(type.equals("Kick")) {
					if (this.id == 0)
						kick(command);
				} else if(type.equals("Ready")) {
					sync();
				} else if(type.equals("Connect Request")) {
					if (this.id == 0)
						initialize(command);
					else
						authorize(command);
				} else if(type.equals("Drawing")) {
					broadcast(command, this.id);
				} else if (type.equals("Clear")) {
					interactShapes("clear", null, null);
					broadcast(command, this.id);
				} else if (type.equals("Close")) {
					if (this.id == 0)
						broadcast(command, this.id);
				} else if (type.equals("Open")) {
					updateShapes(command);
					broadcast(command, this.id);
				} else if (type.equals("Approve")) {
					int id = Integer.parseInt((String)command.get("id"));
					// catch io exception here to avoid being interrupted by peers' quit
					try {
						Task.peers[id].initialize(command);
					} catch (IOException iioe) {
						System.out.println("service " + this.id + " failed to join." );
					}
				} else if (type.equals("Deny")) {
					int id = Integer.parseInt((String)command.get("id"));
					// catch io exception here to avoid being interrupted by peers' quit
					try {
						Task.peers[id].deny();
					} catch (IOException iioe) {
						System.out.println("service " + this.id + " has been denied by the host." );
					}
				}
				
			} catch (IOException ioe) {
				try {
					// To avoid somebody send message and fail when editing quit message.
					this.isReady = false;
					quit();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			} catch (ParseException pe) {
				System.err.println("service " + this.id + " received an invalid message." );
			}
		}
		this.isReady = false;	
	}
	
	/**
	 * update cloud copy and broadcast the new shape update
	 */
	public void newShape(JSONObject shape) throws IOException {
		interactShapes("write", shape, null);
		broadcast(shape, this.id);
	}
	
	/**
	 * update cloud copy with new shapes when host open a new file
	 */
	public void updateShapes(JSONObject command) throws ParseException {
		JSONArray content = (JSONArray)this.parser.parse((String) command.get("content"));
		interactShapes("update", null, content);
	}
	
	/**
	 * ask for host to authorize the connection
	 */
	@SuppressWarnings("unchecked")
	public void authorize(JSONObject command) {
		command.put("id", ""+this.id);
		try {
			Task.peers[0].send(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * grant new visitor an id and broadcast new visitor to all clients
	 */
	@SuppressWarnings("unchecked")
	public void initialize(JSONObject command) throws IOException {
		
		this.username = (String)command.get("username");
		// grant an id
		JSONObject response = new JSONObject();
		response.put("type", "Connect Success");
		response.put("id", ""+this.id);
		response.put("detail", "Accept: your id is " + this.id + ".");
		this.output.writeUTF(response.toJSONString());
		// broadcast new visitor
		JSONObject message = new JSONObject();
		message.put("type", "New Visitor");
		message.put("id", ""+this.id);
		message.put("username", this.username);
		broadcast(message, this.id);
	}	
	
	/**
	 * deny the connection request from current client and terminate the service
	 */
	@SuppressWarnings("unchecked")
	public void deny() throws IOException {
		JSONObject response = new JSONObject();
		response.put("type", "Connect Fail");
		response.put("detail", "The remote host rejected your connection request. Service terminated.");
		send(response);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		terminateService();
	}
	
	/**
	 * broadcast the quit of current client and broadcast quit to all clients
	 */
	@SuppressWarnings("unchecked")
	public void quit() throws IOException {
		
		JSONObject message = new JSONObject();
		message.put("type", "Del Visitor");
		message.put("id", ""+this.id);
		broadcast(message, this.id);
		
		socket.close();
	}
	
	/**
	 * kick out a certain client and response to host immediately
	 */
	public void kick(JSONObject command) throws IOException {
		
		int id = Integer.parseInt((String)command.get("id"));
		Task.peers[id].send(command);
		// the IOException can also be caught by host, which can be used as confirmation
		try {
			Task.peers[id].terminateService();
		} catch (IOException ioe) {
			System.out.println("Service " + id + " has been terminated by the host.");
		}
	}
	
	/**
	 * update the shapes opened by the host to all clients
	 */
	@SuppressWarnings("unchecked")
	public void sync() throws IOException {
		this.isReady = true;
		JSONArray shapes = interactShapes("read", null, null);
		JSONObject update = new JSONObject();
		update.put("type", "Open");
		update.put("content", shapes.toJSONString());
		send(update);
		
		for (int i=0; i<Task.peers.length; i++) {
			if (Task.peers[i].isAlive() && i != this.id) {
				JSONObject message = new JSONObject();
				message.put("type", "New Visitor");
				message.put("username", Task.peers[i].getUsername());
				message.put("id", ""+i);
				send(message);
			}
		}
	}
	
	/**
	 * terminate the service for current client
	 */
	public void terminateService() throws IOException {
		this.socket.close();
	}
	
	/**
	 * bind the socket
	 */
	public void bindSocket(Socket socket) throws IOException {
		this.socket = socket;
		this.input = new DataInputStream(socket.getInputStream());
		this.output = new DataOutputStream(socket.getOutputStream());
	}
	
	/**
	 * share peer list
	 */
	public static void bindPeers(Task[] peers) {
		Task.peers = peers;
	}
	
	/**
	 * share shapes copy
	 */
	public static void initShapes() {
		Task.shapes = new JSONArray();
	}
	
	/**
	 * manipulate the cloud shape copy
	 * @param operation operation type: read, write clear and update
	 * @param shape the shape to be write, null for other modes
	 * @param newShapes the shapes to be update, null for other modes
	 * @return the cloud array in read mode, null for other modes
	 */
	@SuppressWarnings("unchecked")
	public synchronized static JSONArray interactShapes(String operation, JSONObject shape, JSONArray newShapes) {
		if(operation.equals("read")) {
			return Task.shapes;
		} else if (operation.equals("write")){
			Task.shapes.add(shape);
			return null;
		} else if (operation.equals("clear")){
			Task.shapes.clear();
			return null;
		} else {
			Task.shapes = newShapes;
			return null;
		}
	}
	
	/**
	 * send a message to current bound client
	 * @param message the message content
	 */
	public synchronized void send(JSONObject message) throws IOException {
		if(this.isReady)
			this.output.writeUTF(message.toJSONString());
	}
	
	/**
	 * broadcast the message to all ready threads except self
	 * @param message the message to be broadcast
	 * @param id the id of self
	 */
	public synchronized static void broadcast(JSONObject message, int id) throws IOException {
		for (int i=0; i<Task.peers.length; i++) {
			if (i != id) {
				Task.peers[i].send(message);
			}
		}
	}
	
	/**
	 * get the username of current bound client
	 * @return client's username
	 */
	public String getUsername() {
		return this.username;
	}
	
}
