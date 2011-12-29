package com.scada.server.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.scada.utils.Command;
import com.scada.utils.ProtocolUtils;
import com.scada.utils.Response;

public class CHSysInfo extends HandlerBase {
	private static Map<String,Long> fileModifiedList;
	
	
	public CHSysInfo(int id) { 
		super(ProtocolUtils.COMMAND_SYSINFO, id);
		fileModifiedList = new HashMap<String, Long>();
	}
	
	@Override
	void processCommand(Command c) {
		//Do stuff to generate response data
		if(!c.getCommandType().equals(super.getCommandType())) {
			System.out.println("Command Type missmatch in CommandHandler " + super.getCommandType() + ". Command not handled");
		}
		else {
			checkForUpdatedData();
			responseQueue.addAll(createResponseList());
			//fireEvent(createResponseList());
			//return createResponseList();
		}
	}
	
	private List<Response> createResponseList() {
		List<Response> l = new LinkedList<Response>();
		for (Map.Entry<String, String> entry : SysInfoData.getData().entrySet()) {
			Response r = new Response(ProtocolUtils.COMMAND_SYSINFO);
			r.setKey(entry.getKey());
			r.setValue(entry.getValue());
        	l.add(r);
        }
		return l;
	}
	
	private void checkForUpdatedData() {
		File directory = new File("D:\\DEV\\DUMPDIR\\");
		File files[] = directory.listFiles();
		for (File f : files) {   
			if(!fileModifiedList.containsKey(f.getName())) {
				parseAndupdateFileModifiedStamp(f);
			}
			else {
				// Only update if file has changed since last check
				if( fileModifiedList.get(f.getName()) < f.lastModified() ) {
					parseAndupdateFileModifiedStamp(f);
				}
			}
		}	
	}
	
	private void parseAndupdateFileModifiedStamp(File file) {
		parseResultFile(file);
		synchronized(fileModifiedList) {
			fileModifiedList.put(file.getName(), file.lastModified());
		}
	}
	
	private void parseResultFile(File file) {
		FileReader fr = null;
	    BufferedReader br = null;
	 
	    try {
	       fr = new FileReader(file);
	       br = new BufferedReader(fr);
	       
	       String line = null;
	       Map<String,String> m = new HashMap<String, String>();
	       while ((line = br.readLine()) != null) {
	    	   String[] keyValue = handleResultLine(line); 
	    	   if( keyValue != null )
	    		   m.put(keyValue[0], keyValue[1]);
	       }
	       
	       synchronized(SysInfoData.class) {
	    	   SysInfoData.setData(m);
	       }
	       
	    } catch (FileNotFoundException e) {
	       e.printStackTrace();
	    } catch (IOException e) {
	       e.printStackTrace();
	    } finally {
	    	try {
	    		fr.close();
	    		br.close();
	    	} catch (IOException e) {
	 	       fr = null;
	 	       br = null;
		    }
	    }
	}
	
	private String[] handleResultLine(String line) {
		Pattern p = Pattern.compile("\\w++={1}+\\S++");
		Matcher m = p.matcher(line);
		String[] keyValueSplit = null;
		
		if(m.find()) {
			keyValueSplit = m.group().split("=");
		}
		return keyValueSplit;
	}
	
	static class SysInfoData {
		private static Map<String,String> sysInfoData;
		
		public static void setData(Map<String,String> data) {
			if( sysInfoData == null) {
				sysInfoData = new HashMap<String,String>();
			}
			
			synchronized(sysInfoData) {
				sysInfoData.putAll(data);
			}
		}
		
		public static Map<String,String> getData() {
			if( sysInfoData == null) {
				return null;
			}
			
			synchronized(sysInfoData) {
				return sysInfoData;
			}
		}
	}
}
