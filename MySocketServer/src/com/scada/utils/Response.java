package com.scada.utils;

public class Response {
	private String responseType;
	private String key;
	private String value;
	
	public Response(String type) {
		responseType = type;
	}
	
	public String getCommandType() { return responseType; }
	
	public String getKey() { return key; }
	public void setKey( String key) { this.key = key; }
	public String getValue() { return value; }
	public void setValue( String value) { this.value = value; }
}
