package com.scada.server.handlers;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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
		else {
			File directory = new File("D:\\DEV\\DUMPDIR\\");
			File files[] = directory.listFiles();
			for (File f : files) {   
				parseFile(f);
			}	
		}
	}
	
	private void parseFile(File file) {
		FileInputStream fis = null;
	    BufferedInputStream bis = null;
	    DataInputStream dis = null;
	 
	    try {
	       fis = new FileInputStream(file);
	       bis = new BufferedInputStream(fis);
	       dis = new DataInputStream(bis);
	 
	       while (dis.available() != 0) {
	    	   System.out.println(dis.readLine());
	       }
	    } catch (FileNotFoundException e) {
	       e.printStackTrace();
	    } catch (IOException e) {
	       e.printStackTrace();
	    } finally {
	    	try {
	    		fis.close();
	    		bis.close();
	    		dis.close();
	    	} catch (IOException e) {
	 	       fis = null;
	 	       bis = null;
	 	       dis = null;
		    }
	    }
	}
	
	
}
