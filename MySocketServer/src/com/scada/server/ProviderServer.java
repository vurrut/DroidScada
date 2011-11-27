package com.scada.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;

public class ProviderServer extends Thread{
	ServerSocket m_ServerSocket;
	private int serverPort;
	private static int clientID = 0;
	private Vector<ClientThread> connectionTable;
	private boolean serverRunning = false;

	public ProviderServer( int port ) {
		connectionTable = new Vector<ClientThread>();
		serverRunning = true;
		serverPort = port;
		
		try {
			m_ServerSocket = new ServerSocket(serverPort);
		} catch (IOException ioe) {
			System.out.println("Could not create server socket at "	+ serverPort + ". Quitting...");
			serverRunning = false;
			connectionTable = null;
		}

		System.out.println("Listening for clients on " + serverPort);
	}
	
	public void stopServer() {
		Iterator<ClientThread> iter = connectionTable.iterator();
		while(iter.hasNext()) {
			ClientThread c = iter.next();
			c.terminateClientConnection();
		}
		connectionTable.clear();
		connectionTable = null;
		serverRunning = false;
		System.out.println("ProviderServerHost has stopped the ProviderServer");
	}
	
	public void run() {
		while (serverRunning) {
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
}
