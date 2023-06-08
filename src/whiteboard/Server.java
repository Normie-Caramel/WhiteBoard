package whiteboard;

/***
 * Name: Jie Yang
 * Student ID: 1290106
 * E-mail: jieyang3@student.unimelb.edu.au
 */

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.simple.JSONObject;

/**
 * Server used to maintain communications and store public resources
 */
public class Server implements Runnable{

	private final int MAX_VISITOR = 10;
	private int port;
	private Task[] tasks;
	
	/***
	 * Initialize the server
	 * @param port the port server keeps listening on
	 */
	public Server(int port) {
		this.port = port;
		this.tasks = new Task[MAX_VISITOR];
		// Initialize with dummy thread (will be replaced later)
		for (int i=0; i<this.tasks.length; i++) {
			tasks[i] = new Task(i);
		}
		// share the public resources
		Task.bindPeers(this.tasks);
		Task.initShapes();
	}
	

	/**
	 * start the service
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try(ServerSocket ss = new ServerSocket(this.port)){
			System.out.println("Server is listening on port: " + port + "...");
			// keep listening on new request.
			while (true) {
				Socket socket = ss.accept();	
				System.out.println("connected from " + socket.getRemoteSocketAddress());
				
				// check if there is an available position
				boolean positionAvailable = false;
				for (int i=0; i<this.tasks.length; i++) {
					if(!tasks[i].isAlive()) {
						positionAvailable = true;
						tasks[i] = new Task(i, socket);
						tasks[i].start();
						break;
					}
				}
				
				// if all threads are busy, server will reject the request directly
				if(!positionAvailable) {
					DataOutputStream output = new DataOutputStream(socket.getOutputStream());
					JSONObject response = new JSONObject();
					response.put("type", "Connect Fail");
					response.put("detail", "Deny: There is no position left.");
					output.writeUTF(response.toJSONString());
					output.flush();
					socket.close();
				}
			}
		} catch (BindException e) {
			System.out.println("port has been occupied, please try another port.");
		} catch (IOException ioe) {
			System.err.println("data stream failure, reboot in progress...");
		} 
		
	}
	
}
