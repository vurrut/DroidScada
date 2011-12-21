package com.scada.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;

public class StreamManager {
	public BufferedReader StreamReader;
	public BufferedWriter StreamWriter;
	public Socket Socket;
	
	public StreamManager(Socket s, BufferedReader r, BufferedWriter w) {
		Socket = s;
		StreamReader = r;
		StreamWriter = w;
	}
}
