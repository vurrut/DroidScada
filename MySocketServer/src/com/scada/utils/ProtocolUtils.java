package com.scada.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.scada.server.handlers.SaxHandler;

public class ProtocolUtils {
	public final static String XML_DOC_START = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";
	public final static String MESSAGE_START = "message";
	public final static String COMMAND_START = "command";
	public final static String RESPONSE_START = "response";
	public final static String COMMAND_TYPE_ATR = "type";
	public final static String HC_TERMINATE = "<HC_TERMINATE>";

	public final static String COMMAND_SYSINFO = "C_SYSINFO_C";

	private SaxHandler saxHandler;

	public ProtocolUtils() {
		saxHandler = new SaxHandler();
	}

	public boolean parseMessageAndCreateCommands(String xml) {
		//get a factory
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {
				SAXParser sp = spf.newSAXParser();
				InputSource is = new InputSource(new StringReader(xml));
				sp.parse(is, saxHandler);

		}catch(SAXException se) {
			return false;
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch (IOException ie) {
			ie.printStackTrace();
		} 
		return true;
	}

	/**
	 * Get commands from handler command queue
	 * @return
	 */
	public Iterator<Command> getCommands() {
		return saxHandler.getCommandList();
	}
	
	/**
	 * Returns next command from handler queue and at the same time removes it from the queue
	 * @return
	 */
	public Command getNextCommand() {
		return saxHandler.getNextCommand();
	}

	/**
	 * Create message from one command
	 * @param command
	 * @return
	 */
	public String createCommandMessage(Command command) {
		Vector<Command> cL = new Vector<Command>();
		cL.add(command);
		return createCommandMessage(cL);
		
	}
	
	/**
	 * Create Serialized XML message from list of Commands 
	 * @param commands
	 * @return
	 */
	public String createCommandMessage(Vector<Command> commands) {
		Document doc = createXMLDocument();
		doc = createCommandDOMTree(doc, commands);
		return serializeXMLToString(doc);
	}
	
	/**
	 * Create message from one response
	 * @param response
	 * @return
	 */
	public String createResponseMessage(Response response) {
		Vector<Response> rL = new Vector<Response>();
		rL.add(response);
		return createResponseMessage(rL);
		
	}
	
	/**
	 * Create Serialized XML message from list of Responses 
	 * @param response list
	 * @return
	 */
	public String createResponseMessage(Vector<Response> responseList) {
		Document doc = createXMLDocument();
		doc = createResponseDOMTree(doc, responseList);
		return serializeXMLToString(doc);
	}
	
	/**
	 * Helper method to serialize XML content to string.
	 * @param doc
	 * @return
	 */
	private String serializeXMLToString(Document doc) {
		StringWriter writer = null;
		
		try{
			DOMSource domSource = new DOMSource(doc);
			writer = new StringWriter();
			StreamResult streamResult = new StreamResult(writer);
			
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer serializer = tf.newTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
			serializer.setOutputProperty(OutputKeys.INDENT, "no");
			serializer.transform(domSource, streamResult);
			}catch( Exception e){
				System.out.println("Error occured when trying to serialize xml doucment to string representation");
			}
		return writer.toString();
	}

	/**
	 * Using JAXP in implementation independent manner create a document object
	 * using which we create a xml tree in memory
	 * @return
	 */
	private Document createXMLDocument() {
		Document doc = null;
		// get an instance of factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			// get an instance of builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			// create an instance of DOM
			doc = db.newDocument();

		} catch (ParserConfigurationException pce) {
			// dump it
			System.out.println("Error while trying to instantiate DocumentBuilder "	+ pce);
		}
		return doc;
	}

	/**
	 * Create Doom tree containing commands
	 * @param doc
	 * @param commands
	 * @return
	 */
	private Document createCommandDOMTree(Document doc, Vector<Command> commands){
		Element rootEle = doc.createElement(ProtocolUtils.MESSAGE_START);
		doc.appendChild(rootEle);

		//No enhanced for
		Iterator<Command> it  = commands.iterator();
		while(it.hasNext()) {
			Command c = (Command)it.next();
			Element commandElement = createCommandElement(doc,c);
			rootEle.appendChild(commandElement);
		}
		return doc;
	}
	
	/**
	 * Helper method which creates a XML element 

	 * @param c The command for which we need to create an xml representation
	 * @return XML element snippet representing a command
	 */
	private Element createCommandElement(Document doc, Command c){

		Element commandElement = doc.createElement(ProtocolUtils.COMMAND_START);
		commandElement.setAttribute(ProtocolUtils.COMMAND_TYPE_ATR, c.getCommandType());

		//Implement command building
		
		return commandElement;
	}
	
	
	/**
	 * Create Doom tree containing responses
	 * @param doc
	 * @param responses
	 * @return
	 */
	private Document createResponseDOMTree(Document doc, Vector<Response> responses){
		Element rootEle = doc.createElement(ProtocolUtils.MESSAGE_START);
		doc.appendChild(rootEle);

		//No enhanced for
		Iterator<Response> it  = responses.iterator();
		while(it.hasNext()) {
			Element responseElement = createResponseElement(doc,(Response)it.next());
			rootEle.appendChild(responseElement);
		}
		return doc;
	}
	
	/**
	 * Helper method which creates a XML element 

	 * @param c The command for which we need to create an xml representation
	 * @return XML element snippet representing a command
	 */
	private Element createResponseElement(Document doc, Response r){

		Element responseElement = doc.createElement(ProtocolUtils.RESPONSE_START);
		responseElement.setAttribute(ProtocolUtils.COMMAND_TYPE_ATR, r.getCommandType());

		//Implement command building
		
		return responseElement;
	}


}
