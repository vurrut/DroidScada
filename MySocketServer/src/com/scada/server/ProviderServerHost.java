package com.scada.server;

public class ProviderServerHost {
	static ProviderServer s;
	static Thread providerServerThread;
	
	public static void main(String[] args) {
		providerServerThread = new Thread(s = new ProviderServer(12111));
		providerServerThread.setName("ProviderServerThread");
		providerServerThread.setDaemon(true);
		providerServerThread.start();
		
		try{Thread.sleep(20000);}
		catch(Exception ex){}
		//while (true);
		s.stopServer();
	}
}
