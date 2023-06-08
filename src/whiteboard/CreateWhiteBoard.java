package whiteboard;

/***
 * Name: Jie Yang
 * Student ID: 1290106
 * E-mail: jieyang3@student.unimelb.edu.au
 */

import java.awt.EventQueue;

/**
 * host program entry
 */
public class CreateWhiteBoard {

	/**
	 * start the server and whiteboard sequentailly
	 */
	public static void main(String[] args) {
		
		// check arguments' validation
		if (args.length != 3) {
			System.out.println("Invalid arguments, please follow the format:\n <serverIPAddress> <serverPort> <username>");
			System.exit(0);
		}	
		try {
			String ip = args[0];
			int port = Integer.parseInt(args[1]);
			String username = args[2];
			
			if (port < 0 || port > 65535)
				throw new NumberFormatException();
			
			// start the server
		    Thread service = new Thread(new Server(port));
		    service.start();
		    
		    // wait a bit until server finish initialization
		    try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		    
		    // start the whiteboard of host
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					WhiteBoard wb = new WhiteBoard(username, true, ip, port);
					wb.start();
				}
			});
			
		} catch (NumberFormatException e) {
			System.out.println("Please provide valid port number.");
		}
	    
	    	
	}
}
