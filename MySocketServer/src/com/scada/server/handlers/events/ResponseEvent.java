package com.scada.server.handlers.events;

import java.util.EventObject;
import java.util.List;

import com.scada.utils.Response;

public class ResponseEvent extends EventObject{
	private static final long serialVersionUID = 1L;
	public List<Response> responses;

	public ResponseEvent(Object source, List<Response> responseList) {
		super(source);
		responses = responseList;
	}
}
