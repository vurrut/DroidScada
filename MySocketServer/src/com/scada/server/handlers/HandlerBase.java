package com.scada.server.handlers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.scada.server.handlers.events.ResponseEventListener;
import com.scada.utils.Command;
import com.scada.utils.ProtocolUtils;

public abstract class HandlerBase {
	private final String commandType;
	protected Queue<Command> commandQueue;
	protected List<ResponseEventListener> responseEventListeners;
	
	private boolean runFlag = false;
	private HandlerProcessor handlerProcessor;
	private Thread handlerProcessorThread;
	
	public HandlerBase(String type) {
		commandType = type;
		commandQueue = new LinkedList<Command>();
		responseEventListeners = new ArrayList<ResponseEventListener>();
		
		runFlag = true;
		handlerProcessorThread = new Thread(handlerProcessor = new HandlerProcessor());
		handlerProcessorThread.setName("handlerProcessorThread-" + commandType);
		handlerProcessorThread.setDaemon(true);
		handlerProcessorThread.start();
	}
	
	public String getCommandType() {
		return commandType;
	}
	
	public void handleCommand(Command c) {
		commandQueue.add(c);
	}
	
	public synchronized void addEventListener(ResponseEventListener listener) {
		if(!responseEventListeners.contains(listener))
			responseEventListeners.add(listener);
	}
	
	public synchronized void removeEventListener(ResponseEventListener listener) {
		responseEventListeners.remove(listener);
	}
	
	public synchronized void notifyProcessor() {
		synchronized (handlerProcessor) {
			handlerProcessor.notifyAll();
		}
	}
	
	class HandlerProcessor extends Thread {
		public HandlerProcessor() {
			System.out.println("Starting HandlerProcessor for handler: " + ProtocolUtils.COMMAND_SYSINFO);
		}
		int cycle = 1;
		public void run() {
			Command nextCommand;
			
			while(runFlag)
			{
				System.out.println("HandlerProcessor Cycle: " + cycle++);
				nextCommand = commandQueue.poll();
				
				if(nextCommand != null) {
					processCommand(nextCommand);
				}
				else {
					synchronized(this) {
						try { wait(); } catch(InterruptedException ie) {
							//TODO: What to do here?
						}
					}
				}
			}
			System.out.println("Terminating HandlerProcessor for handler: " + ProtocolUtils.COMMAND_SYSINFO);
		}
	}
	
	abstract void processCommand(Command c);

}
