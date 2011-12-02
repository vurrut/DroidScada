package com.scada.server.handlers;

import java.util.List;

import com.scada.utils.Command;
import com.scada.utils.Response;

public abstract class HandlerBase {
	private final String commandType;
	
	public HandlerBase(String type) {
		commandType = type;
	}
	
	public String getCommandType() {
		return commandType;
	}
	
	abstract List<Response> handleCommand( Command c );
}
