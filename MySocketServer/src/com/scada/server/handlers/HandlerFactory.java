package com.scada.server.handlers;

import java.util.HashMap;
import java.util.Map;

import com.scada.server.handlers.events.ResponseEventListener;
import com.scada.utils.ProtocolUtils;

public class HandlerFactory {
	Map<String,Object> handlers;
	
	public HandlerFactory() {
		handlers = new HashMap<String, Object>();
	}
	
	public Object getHandler(String handlerIdentifier, ResponseEventListener rel) {
		return createAndReturnHandler(handlerIdentifier, rel);
	}	
	
	private Object createAndReturnHandler(String handlerIdentifier, ResponseEventListener rel) {
		if(handlers.containsKey(handlerIdentifier))
			return handlers.get(handlerIdentifier);
		else {
			Object handler = null;
			if( handlerIdentifier.equals(ProtocolUtils.COMMAND_SYSINFO))
				handler = new CHSysInfo();
			
			handlers.put(handlerIdentifier, handler );
			((HandlerBase)handler).addEventListener(rel);
			return handler;
		}
	}
	
	private void addListener() {
		
	}
}
