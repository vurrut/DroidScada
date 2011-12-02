package com.scada.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Vector;

import com.scada.server.ClientThread.CommandDispatcher;
import com.scada.server.ClientThread.ResponseDispatcher;
import com.scada.server.handlers.CHSysInfo;
import com.scada.utils.Command;
import com.scada.utils.ProtocolUtils;
import com.scada.utils.Response;

// A client for our multithreaded EchoServer. 
public class EchoClient {
	private static String hostname;
	
	static BufferedReader oIS = null;
	static BufferedWriter oOS = null;
	
	private static boolean receiverThreadRunning = false;
	private static ResponseReceiver responseReceiver;
	private static Thread responseReceiverThread;
	private static boolean clientRunFlag = true;
	private static ProtocolUtils pu;
				
	public static void main(String[] args) {
		pu = new ProtocolUtils();
		
		responseReceiver = new ResponseReceiver();
		responseReceiverThread = new Thread(responseReceiver);
		responseReceiverThread.setName("responseReceiverThread");
		responseReceiverThread.setDaemon(true);
		receiverThreadRunning = true;
		
		
		// First parameter has to be machine name
		if (args.length == 0) {
			// System.out.println("Usage : EchoClient <serverName>");
			// return;
			hostname = "localhost";
		} else {
			hostname = args[0];
		}

		Socket s = null;

		// Create the socket connection to the EchoServer.
		try {
			s = new Socket(hostname, 12111);
		} catch (UnknownHostException uhe) {
			// Host unreachable
			System.out.println("Unknown Host :" + args[0]);
			s = null;
		} catch (IOException ioe) {
			// Cannot connect to port on given host
			System.out
					.println("Cant connect to server at 12111. Make sure it is running.");
			s = null;
		}

		if (s == null)
			System.exit(-1);

		

		try {
			// Create the streams to send and receive information
			oOS = new BufferedWriter(new OutputStreamWriter(
					s.getOutputStream()));
			oOS.flush();

			oIS = new BufferedReader(new InputStreamReader(
					s.getInputStream()));
			
			for( int i = 0; i < 10; i++)
			{
				Vector<Command> test = new Vector<Command>();
				test.add(new Command(ProtocolUtils.COMMAND_SYSINFO));
				String message = pu.createCommandMessage(test);
				oOS.write(message + "\n");
				oOS.flush();
			}
			
			try{Thread.sleep(10000);}
			catch(Exception e){}
			
			for( int i = 0; i < 5; i++)
			{
				Vector<Command> test = new Vector<Command>();
				test.add(new Command(ProtocolUtils.COMMAND_SYSINFO));
				String message = pu.createCommandMessage(test);
				oOS.write(message + "\n");
				oOS.flush();
			}
			
			
			//oOS.write(ProtocolUtils.HC_TERMINATE + "\n");
			//oOS.flush();
			responseReceiverThread.start();
			while(clientRunFlag) {
			
			}
		} catch (IOException ioe) {
			System.out
					.println("Connection to server closed unexpectedly");
		} finally {
			try {
				
				// Close the streams
				if (oOS != null)
					oOS.close();
				if (oIS != null)
					oIS.close();
				// Close the socket before quitting
				if (s != null)
					s.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	static class ResponseReceiver extends Thread {
		
		public ResponseReceiver() {
			System.out.println("Staring Response Receiver");
		}

		public void run() {
			String serverMessage = null;
			while(receiverThreadRunning)
			{
				try {
					serverMessage = (String)oIS.readLine();
				} catch (Exception e) {
					System.out.println("Connection closed. Terminating ResponseReceiverThread ");
				} 
				
				System.out.println("Handling new message from server");
				if(serverMessage == null) {
					System.out.println("Server probably closed the connection");
					clientRunFlag = false;
					receiverThreadRunning = false;
				}
				else if(serverMessage.equals(ProtocolUtils.HC_TERMINATE)) {
					System.out.println("Server has sent terminate connection command");
					clientRunFlag = false;
					receiverThreadRunning = false;
				}
			}
			System.out.println("Terminating ResponseReceiverThread");
		}
	}
}
