package com.scada.client;

import java.io.IOException;
import java.util.Vector;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.scada.utils.Command;
import com.scada.utils.ProtocolUtils;

public class MessageProcessorService extends Service {
	private static final String TAG = MessageProcessorService.class.getSimpleName();
	private static final int DELAY = 60000;
	private boolean runFlag = false;
	private CommandProcessor commandProcessor;
	private ResponseProcessor responseProcessor;
	private DSCApplication appObject;
	
		
	/**
	 * Not used in unbound services
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Method runs only when service is created
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		this.appObject = (DSCApplication)getApplication();
		this.commandProcessor = new CommandProcessor();
		this.responseProcessor = new ResponseProcessor();
		
		Log.d(TAG, "OnCreate finished");
	}

	/**
	 * Method runs when service is destroyed
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		this.runFlag = false;
		this.commandProcessor.interrupt();
		this.commandProcessor = null;
		
		this.responseProcessor.interrupt();
		this.responseProcessor = null;
		
		this.appObject.setServiceRunnning(false);
		
		Log.d(TAG, "OnDestroy finished");
	}

	/**
	 * Method runs when service start command is executed
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "OnStartCommand running");
		super.onStartCommand(intent, flags, startId);
		
		if(!runFlag) {
			this.runFlag = true;
			this.commandProcessor.start();
			this.responseProcessor.start();
			this.appObject.setServiceRunnning(true);
		}
		
		return START_STICKY;
	}
	
	/**
	 * Thread that handles outbound commands
	 * @author mm
	 *
	 */
	private class CommandProcessor extends Thread {
		Intent intent;
		final String localTAG = "CommandProcessor"; 
		
		public CommandProcessor() {
			// Give Thread a name
			super("CommandProcessor"); 
		}
		
		@Override
		public void run() {
			MessageProcessorService messageProcessor = MessageProcessorService.this;
			Log.d(localTAG, "CommandProcessor running");
			StreamManager streamManager = appObject.getStreamManager("",0);
			
			while (messageProcessor.runFlag) {
				/**try {*/
					/**if (newUpdates > 0) {
						Log.d(TAG, "We have a new status");
						intent = new Intent(Constants.NEW_STATUS_INTENT);
						intent.putExtra(Constants.NEW_STATUS_EXTRA_COUNT,newUpdates);
						updaterService.sendBroadcast(intent);
					}*/
					/**Command currentCommand = appObject.getNextCommand();
					while (currentCommand != null ) {
						String xmlMessage = appObject.getProtocolUtils().createCommandMessage(currentCommand);
						try {
							streamManager.StreamWriter.write(xmlMessage + "\n");
							streamManager.StreamWriter.flush();
						} catch (IOException ioe) {
							Log.d(localTAG, "Error trying to send message to server");
						}
						
						currentCommand = appObject.getNextCommand();
					}*/
				
					Vector<Command> currentCommand = appObject.getAllCommandsInQueue();
				
					String xmlMessage = appObject.getProtocolUtils().createCommandMessage(currentCommand);
					try {
						streamManager.StreamWriter.write(xmlMessage + "\n");
						streamManager.StreamWriter.flush();
					} catch (IOException ioe) {
						Log.d(localTAG, "Error trying to send message to server");
					}
					
					synchronized (this) {
						try{ wait(); } catch(InterruptedException ie) {
							//TODO: What to do here?
						}
					}
					
					
					//TODO: Implement Synchronization and suspending of thread when there is nothing to do.
					
				/**} catch (InterruptedException e) {
					Log.d(TAG, "CommandProcessor thread interupted");
					updaterService.runFlag = false;
				} */
			}
		}
	}
	
	
	/**
	 * Thread that handles inbound responses
	 * @author mm
	 *
	 */
	private class ResponseProcessor extends Thread {
		Intent intent;
		final String localTAG = "ResponseProcessor"; 
		
		public ResponseProcessor() {
			// Give Thread a name
			super("CommandProcessor"); 
		}
		
		@Override
		public void run() {
			MessageProcessorService messageProcessor = MessageProcessorService.this;
			Log.d(localTAG, "ResponseProcessor running");
			StreamManager streamManager = appObject.getStreamManager("",0);
			String serverMessage = null;
			int cycle = 1;
			
			while (messageProcessor.runFlag) {
				try {
					serverMessage = (String)streamManager.StreamReader.readLine();
				} catch (Exception e) {
					Log.d(localTAG, "Connection closed. Terminating ResponseReceiverThread ");
					messageProcessor.runFlag = false;
				} 
				
				
				Log.d(localTAG, "Handling new message from server: " + cycle++ );
				if(serverMessage == null) {
					Log.d(localTAG, "Server probably closed the connection");
					messageProcessor.runFlag = false;
					//receiverThreadRunning = false;
				}
				else if(serverMessage.equals(ProtocolUtils.HC_TERMINATE)) {
					Log.d(localTAG, "Server has sent terminate connection command");
					messageProcessor.runFlag = false;
					//receiverThreadRunning = false;
				}
			}
		}
	}

}
