package com.scada.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.Queue;

import com.scada.server.handlers.CHSysInfo;
import com.scada.server.handlers.HandlerBase;
import com.scada.server.handlers.HandlerFactory;
import com.scada.server.handlers.events.ResponseEvent;
import com.scada.server.handlers.events.ResponseEventListener;
import com.scada.utils.Command;
import com.scada.utils.ProtocolUtils;
import com.scada.utils.Response;

public class ClientThread extends Thread {
	private int clientID = -1;
	boolean m_bRunThread = true;
	
	private ProtocolUtils pu;
	private HandlerFactory hf;
	private Queue<Command> commandQueue;
	private Queue<Response> responseQueue;
	private Socket clientSocket;
	private BufferedReader bR = null;
	private BufferedWriter bW = null;
	
	boolean dispatcherThreadRunning = false;
	private CommandDispatcher commandDispatcher;
	private ResponseDispatcher responseDispatcher;
	private Thread commandDispatcherThread;
	private Thread responseDispatcherThread;

	ClientThread(Socket s, int clientID) {
		this.pu = new ProtocolUtils();
		this.clientSocket = s;
		this.clientID = clientID;
		this.hf = new HandlerFactory();
		
		this.commandQueue = new LinkedList<Command>();
		this.commandDispatcherThread = new Thread(commandDispatcher = new CommandDispatcher());
		this.commandDispatcherThread.setName("commandDispatcherThread-" + clientID);
		this.commandDispatcherThread.setDaemon(true);
		
		this.responseQueue = new LinkedList<Response>();
		this.responseDispatcherThread = new Thread(responseDispatcher = new ResponseDispatcher());
		this.responseDispatcherThread.setName("responseDispatcherThread-" + clientID);
		this.responseDispatcherThread.setDaemon(true);
		
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
		synchronized(commandQueue) {
			commandQueue.clear();
		}
		synchronized(responseQueue) {
			responseQueue.clear();
		}
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
			responseDispatcherThread.start();
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
							synchronized(commandQueue) {
								commandQueue.add(c);
							}
							c = pu.getNextCommand();
						}
					}
					else {
						handleHCCommand(clientMessage);
					}
					
					synchronized (commandDispatcher) {
						commandDispatcher.notifyAll();
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
			responseQueue = null;
			responseDispatcher = null;
			responseDispatcherThread = null;
			m_bRunThread = false;
		}
	}
	
	class CommandDispatcher extends Thread implements ResponseEventListener {
		
		public CommandDispatcher() {
			System.out.println("Starting CommandDispatcher for clientID: " + clientID);
		}

		public void run() {
			Command nextCommand;
			int cycle = 1;
			while(dispatcherThreadRunning)
			{
				System.out.println("CommandDispatcher Cycle: " + cycle++);
				nextCommand = commandQueue.poll();
								
				if(nextCommand != null) {
					System.out.println("Pulled command from queue");
					
					Object handler = hf.getHandler(nextCommand.getCommandType(), this);
					if( handler != null ) {
						if( handler instanceof CHSysInfo)
							((HandlerBase)handler).handleCommand(nextCommand);
						
						((HandlerBase)handler).notifyProcessor();
					}
					else
						System.out.println("Unknown command. Skipping command " + nextCommand.getCommandType());
				}
				else {
					synchronized(this) {
						try { wait(); } catch(InterruptedException ie) {
							//TODO: What to do here?
						}
					}
				}
			}
			System.out.println("Terminating CommandDispatcherThread for clientID: " + clientID);
		}

		@Override
		public void handleResponseEvent(EventObject e) {
			ResponseEvent event = (ResponseEvent)e;
			for( Response r : event.responses) {
				responseQueue.add(r);
			}
			
			synchronized (responseDispatcher) {
				responseDispatcher.notifyAll();
			}
		}
		
		
	}
	
	class ResponseDispatcher extends Thread {
		
		public ResponseDispatcher() {
			System.out.println("Starting ResponseDispatcher for clientID: " + clientID);
		}

		public void run() {
			Response nextResponse;
			int cycle = 1;
			
			while(dispatcherThreadRunning)
			{
				System.out.println("ResponseDispatcher Cycle: " + cycle++);
				nextResponse = responseQueue.poll();
								
				if(nextResponse != null) {
					System.out.println("Pulled response from queue");
					String responseMessage = pu.createResponseMessage(nextResponse);
					try {
						bW.write(responseMessage + "\n");
						bW.flush();
					}
					catch(IOException ioe) {
						System.out.println("Error sending message to client in ResponseDispatcher for clientID: " + clientID);
					}
				}
				else {
					synchronized(this) {
						try { wait(); } catch(InterruptedException ie) {
							//TODO: What to do here?
						}
					}
				}
			}
			System.out.println("Terminating ResponseDispatcherThread for clientID: " + clientID);
		}
	}
}
