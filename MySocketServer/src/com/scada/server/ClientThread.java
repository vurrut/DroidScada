package com.scada.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import com.scada.server.handlers.CHSysInfo;
import com.scada.server.handlers.HandlerFactory;
import com.scada.utils.Command;
import com.scada.utils.ProtocolUtils;

public class ClientThread extends Thread {
	private int clientID = -1;
	boolean m_bRunThread = true;
	
	private ProtocolUtils pu;
	private HandlerFactory hf;
	private Queue<Command> commandQueue;
	private Socket clientSocket;
	private BufferedReader bR = null;
	private BufferedWriter bW = null;
	
	boolean dispatcherThreadRunning = false;
	private CommandDispatcher commandDispatcher;
	private Thread commandDispatcherThread;

	ClientThread(Socket s, int clientID) {
		this.pu = new ProtocolUtils();
		this.clientSocket = s;
		this.clientID = clientID;
		this.hf = new HandlerFactory();
		this.commandQueue = new LinkedList<Command>();
		this.commandDispatcherThread = new Thread(commandDispatcher = new CommandDispatcher());
		this.commandDispatcherThread.setName("commandDispatcherThread-" + clientID);
		this.commandDispatcherThread.setDaemon(true);
		
		try {
			bW = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			bW.flush();

			bR = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
		} catch (IOException ioe) {
			System.out.println("Error communicating with client. Closing client connection with ID " + clientID);
			m_bRunThread = false;
		}
	}
	
	public void terminateClientConnection() {
		commandQueue.clear();
		m_bRunThread = false;
		
		try { 
			bW.write(ProtocolUtils.HC_TERMINATE + "\n");
			bW.flush();
		}
		catch( IOException ioe )
		{
			System.out.println("Error writing terminate command");
		}
	}

	public void run() {
		System.out.println("Accepted Client : ID - " + clientID + " : Address - " + clientSocket.getInetAddress().getHostName());
		String clientMessage = "";
		
		if(!dispatcherThreadRunning) {
			dispatcherThreadRunning = true;
			commandDispatcherThread.start();
		}
		
		while (m_bRunThread) {
			try {
				clientMessage = (String)bR.readLine();
			} catch (Exception e) {
				System.out.println("Connection closed. Terminating client thred for clientID: " + clientID);
				cleanUpClientConnection();
			} 
			
			if( m_bRunThread ) {
				System.out.println("Handling new message from client");
				if( clientMessage != null ) {	
					
					if( pu.parseMessageAndCreateCommands(clientMessage) )
					{
						Command c = pu.getNextCommand();
						while(c != null){
							commandQueue.add(c);
							c = pu.getNextCommand();
						}
					}
					else {
						handleHCCommand(clientMessage);
					}
				}
				else
				{
					System.out.println("ClientID " + clientID + " probably closed connection unexpectedly");
					cleanUpClientConnection();
				}
			}
		}
		cleanUpClientConnection();
	}
	
	private void handleHCCommand(String command) {
		if(command.equals(ProtocolUtils.HC_TERMINATE)) {
			cleanUpClientConnection();
			System.out.println("Received terminate command from client");
		}
		else {
			System.out.println("Unknown command recieved from client. Discarding command");
		}
	}
		
	private void cleanUpClientConnection() {
		// Clean up
		try {
			if(dispatcherThreadRunning) {
				dispatcherThreadRunning = false;
			}
			bR.close();
			bW.close();
			clientSocket.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			pu = null;
			hf = null;
			commandQueue = null;
			commandDispatcher = null;
			commandDispatcherThread = null;
			m_bRunThread = false;
		}
	}
	
	class CommandDispatcher extends Thread {
		
		public CommandDispatcher() {
		}

		public void run() {
			Command nextCommand;
			while(dispatcherThreadRunning)
			{
				nextCommand = commandQueue.poll();
				if(nextCommand != null) {
					System.out.println("Pulled command from queue");
					
					Object handler = hf.getHandler(nextCommand.getCommandType());
					if( handler != null ) {
						if( handler instanceof CHSysInfo)
							((CHSysInfo)handler).handleCommand(nextCommand);
					}
					else
						System.out.println("Unknown command. Skipping command " + nextCommand.getCommandType());
				}
			}
			System.out.println("Terminating dispatcherThread for clientID: " + clientID);
		}
	}
}
