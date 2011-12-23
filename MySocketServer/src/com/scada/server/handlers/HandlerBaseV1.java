package com.scada.server.handlers;

import java.util.List;

import com.scada.utils.Command;
import com.scada.utils.Response;

public abstract class HandlerBaseV1 {
	private final String commandType;
	
	public HandlerBaseV1(String type) {
		commandType = type;
	}
	
	public String getCommandType() {
		return commandType;
	}
	
	abstract List<Response> handleCommand( Command c );
}
