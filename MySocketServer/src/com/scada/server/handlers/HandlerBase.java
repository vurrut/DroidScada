package com.scada.server.handlers;

import com.scada.utils.Command;

public abstract class HandlerBase {
	private final String commandType;
	
	public HandlerBase(String type) {
		commandType = type;
	}
	
	public String getCommandType() {
		return commandType;
	}
	
	abstract void handleCommand( Command c );
}
