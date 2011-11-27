package com.scada.utils;

public class Response {
	private String responseType;
	
	public Response(String type) {
		responseType = type;
	}
	
	public String getCommandType() { return responseType; }
}
