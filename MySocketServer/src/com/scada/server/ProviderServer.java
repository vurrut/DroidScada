package com.scada.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Vector;


public class ProviderServer{
	private boolean serverRunning = false;
	private int port;
	private int socketTimeout;
	private static int clientID = 0;
	
	private ProviderServerCore serverCore;
	private Thread serverThread;

	public ProviderServer( int port, int socketTimeout ) {
		serverRunning = true;
		this.port = port;
		this.socketTimeout = socketTimeout;
	}
	
	public void stopServer() { 
		System.out.println("Waiting for servercore to shut down before stopping server.");
		serverRunning = false;
	}
	
	public void startServer() { 
		serverRunning = true;
		this.serverThread = new Thread(serverCore = new ProviderServerCore(port, socketTimeout));
		this.serverThread.setName("ProviderServerCore");
		this.serverThread.setDaemon(true);
		serverThread.start();
	}
	
	
	
	class ProviderServerCore extends Thread{
		private ServerSocket m_ServerSocket;
		private int serverPort;
		private Vector<ClientThread> connectionTable;
		private boolean serverStartedSuccessfully = true;

		public ProviderServerCore( int port, int socketTimeout ) {
			connectionTable = new Vector<ClientThread>();
			serverPort = port;
			
			try {
				m_ServerSocket = new ServerSocket(serverPort);
				m_ServerSocket.setSoTimeout(socketTimeout);
				System.out.println("Listening for clients on " + serverPort);
			} catch (IOException ioe) {
				System.out.println("Could not create server socket at "	+ serverPort + ". Quitting...");
				serverRunning = false;
				serverStartedSuccessfully = false;
				connectionTable = null;
			}
		}
		
		public void stopServerCore() { 
			Iterator<ClientThread> iter = connectionTable.iterator();
			while(iter.hasNext()) {
				ClientThread c = iter.next();
				if(c.dispatcherThreadRunning)
					c.terminateClientConnection();
			}
			connectionTable.clear();
			connectionTable = null;
			
			try {
				m_ServerSocket.close();
			} catch(IOException ioe) {
				System.out.println("Error occured when trying to close serversocket.");
			} finally {
				m_ServerSocket = null;
			}
			
			System.out.println("ProviderServerCore shutdown sequence finished");
		}
		
		public void run() {
			while (serverRunning) {
				try {
					Socket clientSocket = m_ServerSocket.accept();

					ClientThread newClient;
					Thread newClientThread = new Thread(newClient = new ClientThread(clientSocket, clientID++));
					newClientThread.setDaemon(true);
					newClientThread.setName("ClientThread-" + (clientID-1));
					newClientThread.start();

					connectionTable.add(newClient);
				} catch (SocketTimeoutException ste) {
					System.out.println("Socket Timed out waiting for client. New client search cycle starting....");
				} catch (IOException ioe) {
					System.out.println("Exception encountered on accept. Ignoring. Stack Trace :");
					ioe.printStackTrace();
				} 
			}
			
			if(serverStartedSuccessfully) {
				System.out.println("Stopping ProviderServer.....");
				stopServerCore();
			}
		}
	}
}
