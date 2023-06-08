package whiteboard;

/***
 * Name: Jie Yang
 * Student ID: 1290106
 * E-mail: jieyang3@student.unimelb.edu.au
 */

import java.awt.EventQueue;

/**
 * client program entry
 */
public class JoinWhiteBoard {

	/**
	 * start the client whiteboard according to given arguments
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

			// start the client whiteboard
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					WhiteBoard wb = new WhiteBoard(username, false, ip, port);
					wb.start();
				}
			});
			
		} catch (NumberFormatException e) {
			System.out.println("Please provide valid port number.");
		}
		
	}
}
