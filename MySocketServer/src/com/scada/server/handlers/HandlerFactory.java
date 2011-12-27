package com.scada.server.handlers;

import java.util.HashMap;
import java.util.Map;

import com.scada.server.handlers.events.ResponseEventListener;
import com.scada.utils.ProtocolUtils;

public class HandlerFactory {
	Map<String,HandlerBase> handlers;
	
	public HandlerFactory() {
		handlers = new HashMap<String, HandlerBase>();
	}
	
	public HandlerBase getHandler(String handlerIdentifier, ResponseEventListener rel) {
		return createAndReturnHandler(handlerIdentifier, rel);
	}	
	
	private HandlerBase createAndReturnHandler(String handlerIdentifier, ResponseEventListener rel) {
		if(handlers.containsKey(handlerIdentifier))
			return handlers.get(handlerIdentifier);
		else {
			HandlerBase handler = null;
			if( handlerIdentifier.equals(ProtocolUtils.COMMAND_SYSINFO))
				handler = new CHSysInfo();
			
			handler.addEventListener(rel);
			
			handlers.put(handlerIdentifier, handler );
						
			return handler;
		}
	}
	
	public synchronized void stopHandlers() {
		for( HandlerBase b : handlers.values()) {
			b.stopHandler();
		}
	}
}
