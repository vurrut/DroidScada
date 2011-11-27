package com.scada.server.handlers;

import java.util.HashMap;
import java.util.Map;

import com.scada.utils.ProtocolUtils;

public class HandlerFactory {
	Map<String,Object> handlers;
	
	public HandlerFactory() {
		handlers = new HashMap<String, Object>();
	}
	
	public Object getHandler(String handlerIdentifier) {
		return createAndReturnHandler(handlerIdentifier);
	}	
	
	private Object createAndReturnHandler(String handlerIdentifier) {
		if(handlers.containsKey(handlerIdentifier))
			return handlers.get(handlerIdentifier);
		else {
			Object handler = null;
			if( handlerIdentifier.equals(ProtocolUtils.COMMAND_SYSINFO))
				handler = new CHSysInfo();
			
			handlers.put(handlerIdentifier, handler );
			return handler;
		}
	}
}
