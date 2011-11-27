package com.scada.server.handlers;

import com.scada.utils.Command;
import com.scada.utils.ProtocolUtils;

public class CHSysInfo extends HandlerBase {
	
	public CHSysInfo() { 
		super(ProtocolUtils.COMMAND_SYSINFO);	
	}
	
	public void handleCommand(Command c) {
		//Do stuff to generate response data
		if(!c.getCommandType().equals(super.getCommandType())) {
			System.out.println("Command Type missmatch in CommandHandler " + super.getCommandType() + ". Command not handled");
		}
	}
	
	
}
