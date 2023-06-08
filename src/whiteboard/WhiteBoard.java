package whiteboard;

/***
 * Name: Jie Yang
 * Student ID: 1290106
 * E-mail: jieyang3@student.unimelb.edu.au
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * The frame of whiteboard
 */
public class WhiteBoard {
	
	private final int BUTTON_SIZE = 28;
	private final int MENU_WIDTH  = 146;
	private final int MENU_HEIGHT = 20;
	private final int GAP_LENGTH  = 10;
	private final int THIN_GAP    = 4;
	private final int MAX_VISITOR = 10;
	private final int ACC_HEIGHT  = 30;
	private final int CHAT_HEIGHT = 10000;
	private final int CONNECT_TIMEOUT = 15000;
	
	private File path = null;
	private boolean isServer;
	private int id;
	private String username;
	private MyCanvas canvas;
	private JFrame frame;
	private JPanel menu;
	private JPanel tools;
	private JPanel people;
	private JTextArea chat;
	private Window prompt;
	private ArrayList<Shape> shapes;
	private ArrayList<JButton> clientButtons;
	private ArrayList<JButton> serverButtons;
	private Account[] accounts;
	
	private String ip;
	private int port;
	private DataInputStream input;
	private DataOutputStream output;
	private JSONParser parser = new JSONParser();
	
	public WhiteBoard(String username, boolean isServer, String ip, int port) {
		
		Locale.setDefault(Locale.ENGLISH);

		// Initialize the overall varaibles
		this.frame = new JFrame("Online White Board");
		this.frame.setSize(1000, 600);
		this.frame.setLocationRelativeTo(null);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.getContentPane().setLayout(null);
		
		this.username = username;
		this.isServer = isServer;
		
		this.shapes = new ArrayList<Shape>();
		this.clientButtons = new ArrayList<JButton>();
		this.serverButtons = new ArrayList<JButton>();
		this.accounts = new Account[MAX_VISITOR];
		
		this.ip = ip;
		this.port = port;
		this.id = -1;
		
		// start the client communication
		new Thread(() -> client()).start();
		
		// wait for the server to grant an id, check once per 0.1s.
		try {
			int timeCount = 0;
			while(this.id < 0 && timeCount < CONNECT_TIMEOUT) {
				Thread.sleep(100);
				timeCount += 100;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// if there is no response, the program will terminate
		if(this.id < 0)
			System.exit(0);
		
		// initialzie specific features
		initialize();

	}
	
	private void initialize() {
		// Initialize the six elemnets and their own components
		this.canvas = new MyCanvas();
		this.menu   = new JPanel();
		this.tools = new JPanel();
		this.people = new JPanel();
		this.chat   = new JTextArea();
		this.prompt = new Window();
		
		// bind the elements with shared resources
		this.canvas.setShapes(this.shapes);
		this.prompt.bindChat(this.chat, this.username);
		this.prompt.bindOutput(this.output);
		
		this.menu.setBounds(10, 10, 770, 20);
		this.canvas.setBounds(80, 40, 700, 470);
		this.tools.setBounds(10, 40, 60, 470);
		this.people.setBounds(790, 10, 180, 300);
		this.chat.setBounds(790, 320, 180, CHAT_HEIGHT);
		
		this.canvas.setBackground(Color.WHITE);
		this.people.setBackground(Color.WHITE);
		
		this.frame.getContentPane().add(menu);
		this.frame.getContentPane().add(canvas);
		this.frame.getContentPane().add(tools);
		this.frame.getContentPane().add(people);
		this.frame.getContentPane().add(prompt.init());
		
		// List buttons in each of the function areas
		String[] menuBars = {"new", "open", "save", "saveAs", "close"};
		String[] shapes = {"Line", "Cir.", "Tri.", "Rect", "Text"};
		Color[] colors = {
				new Color(0, 0, 0), new Color(255, 255, 255), new Color(128,0,0), new Color(128,64,0), new Color(255,0,0), new Color(255,128,192),
				new Color(255,128,0), new Color(255,255,0), new Color(0,128,64), new Color(128,255,128), new Color(0,128,255), new Color(128,255,255), 
				new Color(0,0,160), new Color(128,128,192), new Color(128,0,255), new Color(255,0,128), new Color(128,0,128), new Color(0,128,128)};
		
		// Initialize the menu components
		this.menu.setLayout(null);
		for (int i=0; i<menuBars.length; i++) {
			JButton bt = new JButton(menuBars[i]);
			int x = i * (MENU_WIDTH + GAP_LENGTH);
			bt.setBounds(x, 0, MENU_WIDTH, MENU_HEIGHT);
			bt.setBackground(Color.LIGHT_GRAY);
			this.serverButtons.add(bt);
			this.menu.add(bt);
		}
		
		// Initialize the tool (color) components
		this.tools.setLayout(null);
		for (int i=0; i<colors.length; i+=2) {
			JButton btLeft = new JButton();
			JButton btRight = new JButton();
			btLeft.setBackground(colors[i]);
			btRight.setBackground(colors[i+1]);
			int y = i / 2 * (BUTTON_SIZE + THIN_GAP);
			btLeft.setBounds(0, y, BUTTON_SIZE, BUTTON_SIZE);
			btRight.setBounds(0+BUTTON_SIZE+THIN_GAP, y, BUTTON_SIZE, BUTTON_SIZE);
			this.clientButtons.add(btLeft);
			this.clientButtons.add(btRight);			
			this.tools.add(btLeft);
			this.tools.add(btRight);			
		}
		
		// Initialize the tool (shape) components
		int initialY = colors.length / 2 * (BUTTON_SIZE + THIN_GAP);
		int toolButtonWidth = 2 * BUTTON_SIZE + THIN_GAP;
		for (int i=0; i<shapes.length; i++) {
			JButton bt = new JButton(shapes[i]);
			int y = initialY + i * (BUTTON_SIZE + GAP_LENGTH);
			bt.setBounds(0, y, toolButtonWidth, BUTTON_SIZE);
			bt.setBackground(Color.LIGHT_GRAY);
			this.clientButtons.add(bt);
			this.tools.add(bt);
		}
		
		// Initialize the people containers
		this.people.setLayout(null);
		for (int i=0; i<MAX_VISITOR; i++) {
			Account account = new Account();
			account.init(i, output);
			int y = 0 + i * ACC_HEIGHT;
			account.setBounds(0, y, 180, ACC_HEIGHT);
			account.setBackground(Color.WHITE);
			this.people.add(account);
			this.accounts[i] = account;
		}
		
		// Initialize the chat box
		this.chat.setEditable(false);
		this.chat.setWrapStyleWord(true);
		this.chat.setLineWrap(true);
		JScrollPane scroll = new JScrollPane(this.chat);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setBounds(790, 320, 180, 230);
		this.frame.getContentPane().add(scroll);
		
		// initialization based on user identity
		if(this.isServer)
			serverInit();
		else
			clientInit();
	}
	
	/**
	 * initialize the functions for server only
	 */
	private void serverInit() {
		// bind the event listener for menu buttons
		for (JButton bt : this.serverButtons) {
			bt.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					JButton source = (JButton)e.getSource();
					String command = source.getActionCommand();
					switch (command) {
						case "new": newCanvas(); break;
						case "open": open(); break;
						case "save": save(); break;
						case "saveAs": saveAs(); break;
						case "close": close(); break;
						default: break;
					}
				}
			});
		}
		// add the host itself and disable the kick function for itself
		this.accounts[this.id].bind(this.username);
		this.accounts[this.id].disableKick();
	}
	
	/**
	 * initialize the functions for client
	 */
	private void clientInit() {
		// disabled the menu buttons
		for (JButton bt : this.serverButtons) {
			bt.setEnabled(false);
		}
		// disabled the kick functions
		for (Account ac : this.accounts) {
			ac.disableKick();
		}
		// add the client itself
		this.accounts[this.id].bind(this.username);
	}
	
	/**
	 * Activate the program
	 */
	public void start() {
		this.frame.setVisible(true);
		// since graphics object of canvas can only be extracted after it sets to be visible,
		// we bind the customized listener after all component has been activate.
		initialLocalListener();
		
		// after finishing all initialization, send ready to server.
		try {
			ready();
		} catch (IOException e) {
			System.out.println("No response from remote host, please check IP and port...");
		}
	}
	
	/**
	 * Create a listener that keep listening on canvas and tools
	 */
	private void initialLocalListener() {
		Listener lsn = new Listener();
		lsn.setShapes(this.shapes);
		lsn.setPrompt(this.prompt);
		lsn.setPeople(this.id, this.accounts);
		lsn.setOutput(this.output);
		bindListener(lsn);
	}

	/**
	 * Bind the listener to the tool buttons and canvas
	 * @param lsn listener
	 */
	private void bindListener(Listener lsn) {
		this.canvas.addMouseListener(lsn);
		this.canvas.addMouseMotionListener(lsn);
		for (JButton bt : this.clientButtons) {
			bt.addActionListener(lsn);
		}
		Graphics brush = this.canvas.getGraphics();
		lsn.setBrush(brush);
	}
	 
	/**
	 * create a new canvas, which will discard the old one and cannot undo
	 */
	@SuppressWarnings("unchecked")
	private void newCanvas() {
		this.path = null;
		try {
			JSONObject command = new JSONObject();
			command.put("type", "Clear");
			send(command);
		} catch (IOException e) {
			System.out.println("Connection failed.");
		}
		this.shapes.clear();
		this.canvas.repaint();	
	}
	
	/**
	 * open a saved file and update it with server
	 */
	@SuppressWarnings("unchecked")
	private void open() {
		// prompt a choose window
		JFileChooser fc = new JFileChooser();
		FileFilter jsonFilter = getJSONFilter();
		fc.setFileFilter(jsonFilter);
		int option = fc.showDialog(this.frame, "Open");
		// check validation
		if(option == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if(file.getName().toLowerCase().endsWith(".json")) {
				try (FileReader reader = new FileReader(file)){
					// send to server first
					JSONArray record = (JSONArray)this.parser.parse(reader);
					JSONObject update = new JSONObject();
					update.put("type", "Open");
					update.put("content", record.toJSONString());
					send(update);
					// reset the save path
					this.path = file;
					// then repaint locally
					this.shapes.clear();
					record.forEach(s -> rebuild((JSONObject)s));
					this.canvas.repaint();
				} catch (FileNotFoundException e) {
					System.out.println("The specified file is not found.\n");
				} catch (IOException e) {
					this.chat.append("System: IO failed, please try again.\n");
				} catch (ParseException e) {
					this.chat.append("System: Parse failed, invalid storage format.\n");
				}
			} else {
				this.chat.append("System: Open failed, chosen file is of invalid format (JSON only).\n");
			}
		}
	}
	
	/**
	 * transform the JSON object into shapes and push it into shared array
	 * @param s
	 */
	private void rebuild(JSONObject s) {
		Shape shape = decode(s);
		this.shapes.add(shape);
	}
	
	/**
	 * save the current canvas to determined path (if not determined, pass on to saveAs)
	 */
	private void save() {
		if (this.path != null)
			saveFile(this.path);
		else {
			saveAs();
		}
	}
	
	/**
	 * choose a file path to save current canvas
	 */
	private void saveAs() {
		// prompt choose window
		JFileChooser fc = new JFileChooser();
		FileFilter jsonFilter = getJSONFilter();
		fc.setFileFilter(jsonFilter);
		int option = fc.showDialog(this.frame, "Save");
		// check validation and save the file
		if(option == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			if(file.getName().toLowerCase().endsWith(".json")) {
				this.path = file;
				saveFile(file);
			}
			else
				this.chat.append("System: Save failed, please save with JSON format.\n");
		}
	}
	
	/**
	 * get a filefilter which filt other format except json
	 * @return filefilter
	 */
	private FileFilter getJSONFilter() {
		FileFilter jsonFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if(pathname.getName().toLowerCase().endsWith(".json"))
					return true;
				return false;
			}

			@Override
			public String getDescription() {
				return "JSON File(*.json)";
			}
		};
		return jsonFilter;
	}
	
	/**
	 * transfrom the shape array into JSON array and save it into specified path
	 * @param file
	 */
	@SuppressWarnings("unchecked")
	private void saveFile(File file) {
		JSONArray saveShapes = new JSONArray();
		for (Shape s : this.shapes)
			saveShapes.add(encode(s));
		try(FileWriter fw = new FileWriter(file)){
			fw.write(saveShapes.toJSONString());
			fw.flush();
			fw.close();
		} catch (IOException e) {
			System.err.println("Save failure: unable to find the path or open the default file.");
		}
		this.chat.append("System: file has been saved.\n");
	}
	
	/**
	 * close the host whiteboard and terminate the service.
	 */
	@SuppressWarnings("unchecked")
	private void close() {
		try {
			JSONObject command = new JSONObject();
			command.put("type", "Close");
			send(command);
			// wait the message to be delivered
			Thread.sleep(1000);
		} catch (IOException | InterruptedException e) {
			System.out.println("Connection failed.");
		}
		System.exit(0);
	}

	/**
	 * maintain a socket listener listening on channel, handle command accordingly.
	 */
	@SuppressWarnings("unchecked")
	private void client() {
		try (Socket socket = new Socket(ip, port)){
			
			this.input = new DataInputStream(socket.getInputStream());
			this.output = new DataOutputStream(socket.getOutputStream());
			
			// ask for connection at first
			JSONObject connect = new JSONObject();
			connect.put("type", "Connect Request");
			connect.put("username", username);
			output.writeUTF(connect.toJSONString());
			output.flush();
			
			// handle the info from server according to command type
			while (true) {
				JSONObject command = (JSONObject)parser.parse(input.readUTF());
				String type = (String)command.get("type");
				if (type.equals("Connect Success")) {
					String detail = (String)command.get("detail");
					this.id = Integer.parseInt((String)command.get("id"));
					System.out.println(detail);
				} else if (type.equals("Connect Fail")) {
					String detail = (String)command.get("detail");
					System.out.println(detail);
					break;
				} else if (type.equals("New Visitor")) {
					addVisitor(command);
				} else if (type.equals("Del Visitor")) {
					delVisitor(command);
				} else if (type.equals("New Shape")) {
					addShape(command);
				} else if (type.equals("Drawing")) {
					int id = Integer.parseInt((String)command.get("id"));
					this.accounts[id].activate();
				} else if (type.equals("Chat")) {
					addChat(command);
				} else if (type.equals("Clear")) {
					this.shapes.clear();
					this.canvas.repaint();
				} else if (type.equals("Close")) {
					System.out.println("Remote server quit and closed the application, service terminated...");
					break;
				} else if (type.equals("Kick")) {
					System.out.println("You have been kicked out by the host, service terminated");
					break;
				} else if (type.equals("Open")) {
					updateShapes(command);
				} else if (type.equals("Connect Request")) {
					ask(command);
				}
			}
			socket.close();
		} catch (UnknownHostException e) {
			System.out.println("IP address is not resolvable, please check again...");
		} catch (IOException e) {
			System.out.println("No response from remote host, please check IP and port...");
		} catch (ParseException e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}
	
	/**
	 * send ready information to server
	 */
	@SuppressWarnings("unchecked")
	private void ready() throws IOException {
		JSONObject command = new JSONObject();
		command.put("type", "Ready");
		send(command);
	}
	
	/**
	 * add a new visitor to people panel
	 */
	private void addVisitor(JSONObject command) {
		String username = (String)command.get("username");
		int id = Integer.parseInt((String)command.get("id"));
		this.accounts[id].bind(username);
	}
	
	/**
	 * delete a visitor from people panel
	 */
	private void delVisitor(JSONObject command) {
		int id = Integer.parseInt((String)command.get("id"));
		this.accounts[id].unbind();
	}
	
	/**
	 * add a shape to shapes array and repaint
	 */
	private void addShape(JSONObject command) {
		int id = Integer.parseInt((String)command.get("id"));
		this.accounts[id].deactivate();
		Shape newShape = decode(command);
		this.shapes.add(newShape);
		this.canvas.repaint();
	}
	
	/**
	 * add the content to chatbox
	 */
	private void addChat(JSONObject command) {
		String username = (String)command.get("username");
		String content = (String)command.get("content");
		this.chat.append(username + ": " + content + "\n");
	}
	
	/**
	 * replace all the shapes with received JSON array
	 */
	@SuppressWarnings("unchecked")
	private void updateShapes(JSONObject command) throws ParseException {
		JSONArray content = (JSONArray)this.parser.parse((String) command.get("content"));
		this.shapes.clear();
		content.forEach(shape -> rebuild((JSONObject)shape));
		this.canvas.repaint();
	}
	
	/**
	 * show prompt to host to gain the authorization
	 */
	@SuppressWarnings("unchecked")
	private void ask(JSONObject command) throws IOException {
		String username = (String)command.get("username");
		int id = Integer.parseInt((String)command.get("id"));
		int option = JOptionPane.showConfirmDialog(this.frame, username + " wants to share your whiteboard.", "Connection Request", JOptionPane.YES_NO_OPTION);
		if(option == JOptionPane.YES_OPTION) {
			JSONObject response = new JSONObject();
			response.put("type", "Approve");
			response.put("username", username);
			response.put("id", ""+id);
			send(response);
		} else {
			JSONObject response = new JSONObject();
			response.put("type", "Deny");
			response.put("id", ""+id);
			send(response);
		}
	}
	
	/**
	 * send message to peer thread at server side
	 */
	private void send(JSONObject message) throws IOException {
		this.output.writeUTF(message.toJSONString());
	}
	
	/**
	 * decode the json obejct into shape
	 */
	private Shape decode(JSONObject shape) {
		int x1 = Integer.parseInt((String)shape.get("x1"));
		int y1 = Integer.parseInt((String)shape.get("y1"));
		int x2 = Integer.parseInt((String)shape.get("x2"));
		int y2 = Integer.parseInt((String)shape.get("y2"));
		String geo = (String)shape.get("geo");
		int r = Integer.parseInt((String)shape.get("r"));
		int g = Integer.parseInt((String)shape.get("g"));
		int b = Integer.parseInt((String)shape.get("b"));
		String content = "";
		if(geo.equals("Text")) {
			content = (String)shape.get("content");
		}
		Color color = new Color(r,g,b);
		switch(geo) {
			case "Line": return new Line(x1, y1, x2, y2, geo, color);
			case "Cir.": return new Circle(x1, y1, x2, y2, geo, color);
			case "Tri.": return new Triangle(x1, y1, x2, y2, geo, color);
			case "Rect" : return new Rectangle(x1, y1, x2, y2, geo, color);
			case "Text": return new Text(x1, y1, x2, y2, geo, color, content);
			default: return null;
		}
	}
	
	/**
	 * encode the shape into json format
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
