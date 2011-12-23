package com.scada.server.handlers.events;

import java.util.EventObject;

public interface ResponseEventListener {
	public void handleResponseEvent(EventObject e);
}
