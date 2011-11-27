package com.scada.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ProviderServerV1 {
	ServerSocket m_ServerSocket;
	private static final int SERVERPORT = 12111;
	private static int clientID = 0;
	private Vector<ClientThread> connectionTable;

	public ProviderServerV1() {
		connectionTable = new Vector<ClientThread>();

		try {
			// Create the server socket.
			m_ServerSocket = new ServerSocket(SERVERPORT);
		} catch (IOException ioe) {
			System.out.println("Could not create server socket at "
					+ SERVERPORT + ". Quitting...");
			System.exit(-1);
		}

		System.out.println("Listening for clients on " + SERVERPORT);

		// Successfully created Server Socket. Now wait for connections.
		while (true) {
			try {
				// Accept incoming connections (Blocks until new client
				// connects)
				Socket clientSocket = m_ServerSocket.accept();

				ClientThread newClient;
				Thread newClientThread = new Thread(newClient = new ClientThread(clientSocket, clientID++));
				newClientThread.setDaemon(true);
				newClientThread.setName("ClientThread-" + clientID);
				newClientThread.start();

				connectionTable.add(newClient);
			} catch (IOException ioe) {
				System.out
						.println("Exception encountered on accept. Ignoring. Stack Trace :");
				ioe.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new ProviderServerV1();
	}
}
