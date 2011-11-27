package com.scada.utils;

public class Command {
	private String commandType;
	
	public Command(String type) {
		commandType = type;
	}
	
	public String getCommandType() { return commandType; }
}
