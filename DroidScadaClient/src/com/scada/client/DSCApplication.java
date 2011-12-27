package com.scada.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

import android.app.Application;
import android.util.Log;

import com.scada.utils.Command;
import com.scada.utils.ProtocolUtils;
import com.scada.utils.Response;

public class DSCApplication extends Application /**implements OnSharedPreferenceChangeListener*/ {
	private static final String TAG = DSCApplication.class.getSimpleName();
	//private SharedPreferences prefs;
	private boolean serviceRunning;
	private StreamManager streamManager;
	private ProtocolUtils pu;
	private Queue<Command> commandQueue;
	private Queue<Response> responseQueue;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		//this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//this.prefs.registerOnSharedPreferenceChangeListener(this);
		Log.d(TAG, "Application OnCreate finished");
		commandQueue = new LinkedList<Command>();
		responseQueue = new LinkedList<Response>();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.d(TAG, "Application OnTerminate finished");
	}
	
	public ProtocolUtils getProtocolUtils() {
		if(pu == null)
			pu = new ProtocolUtils();
		return pu;
	}
	
	public void addCommandToQueue(Command c) {
		commandQueue.add(c);
	}
	
	public void addCommandCollectionToQueue(Vector<Command> commands) {
		Iterator<Command> i = commands.iterator();
		while(i.hasNext()) {
			commandQueue.add(i.next());
		}
	}
	
	public Command getNextCommand() {
		return commandQueue.poll();
	}
	
	public Vector<Command> getAllCommandsInQueue() {
		Vector<Command> cL = new Vector<Command>();
		Command nextCommand = commandQueue.poll();
		while(nextCommand != null) {
			cL.add(nextCommand);
			nextCommand = commandQueue.poll();
		}
		return cL;
	}

	/**@Override
	public synchronized void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
	}*/
	
	/**public SharedPreferences getSharedPrefs() {
		return prefs;
	}*/
	
	public boolean isServiceRunning() {
		return serviceRunning;
	}
	
	public void setServiceRunnning( boolean serviceRunning ) {
		this.serviceRunning = serviceRunning;
	}
	
	public void terminateStreamManager() {
		try {
			// Close the streams
			if (streamManager.StreamWriter != null)
				streamManager.StreamWriter.close();
			if (streamManager.StreamReader != null)
				streamManager.StreamReader.close();
			// Close the socket before quitting
			if (streamManager.Socket != null)
				streamManager.Socket.close();
		} catch( IOException ioe) {
			System.out.println("Error trying to close streams and socket");
		} finally {
			streamManager = null;
		}
	}
	
	public StreamManager getStreamManager(String ip, int port) {
		if(streamManager == null)
			return configureStreams(ip, port);
		else
			return streamManager;
	}
	
	private StreamManager configureStreams(String ip, int port) {
		BufferedReader streamReader = null;
		BufferedWriter streamWriter = null;
		Socket s = null;
		
		// Create the socket connection to the EchoServer.
		try {
			s = new Socket(ip, port);
		} catch (UnknownHostException uhe) {
			// Host unreachable
			System.out.println("Unknown Host :");
			s = null;
		} catch (IOException ioe) {
			// Cannot connect to port on given host
			System.out.println("Cant connect to server at 12111. Make sure it is running.");
			s = null;
		}
		
		try {
			streamWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			streamWriter.flush();
			streamReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
		} catch( IOException ioe) {
			Log.d(TAG, "Error when configuring streams");
			return null;
		}
		
		streamManager = new StreamManager(s, streamReader, streamWriter);
		return streamManager;
	}
	
}
