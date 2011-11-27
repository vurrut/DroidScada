package com.scada.server.handlers;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.scada.utils.Command;
import com.scada.utils.ProtocolUtils;

public class SaxHandler extends DefaultHandler { 
	Queue<Command> commandQueue = new LinkedList<Command>();
	
	Command newCommand;
	String tempVal;
	//Event Handlers
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if(qName.equalsIgnoreCase("command")) {
			//create a new instance of employee
			newCommand = new Command(attributes.getValue(ProtocolUtils.COMMAND_TYPE_ATR));
		}
	}


	public void characters(char[] ch, int start, int length) throws SAXException {
		tempVal = new String(ch,start,length);
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(qName.equalsIgnoreCase("command")) {
			//add it to the list
			commandQueue.add(newCommand);
		}
		/**else if (qName.equalsIgnoreCase("Name")) {
			tempEmp.setName(tempVal);
		}*/
	}
	
	public Iterator<Command> getCommandList()
	{
		return commandQueue.iterator();
	}
	
	public Command getNextCommand()
	{
		return commandQueue.poll();
	}
}