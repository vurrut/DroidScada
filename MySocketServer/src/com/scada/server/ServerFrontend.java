package com.scada.server;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class ServerFrontend implements ActionListener {
	private JFrame frame = new JFrame("DroidScadaServer - Frontend");
	private JPanel panel = new JPanel();

	private JButton start;
	private JButton stop;
	
	boolean runServer = false;
	private ProviderServer server;

	public ServerFrontend() {
		GridLayout gl = new GridLayout(2, 2);
		gl.setVgap(4);
		panel.setLayout(gl);

		addWidgets();
		
		frame.getContentPane().add(panel, BorderLayout.CENTER);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		
		server = new ProviderServer(12111, 5000);
		startServer();
	}
	
	public static void main(String[] args) {
		// set the look and feel
	    try {
	      UIManager.setLookAndFeel(UIManager
	          .getCrossPlatformLookAndFeelClassName());
	    } catch (Exception e) {
	    }

	    ServerFrontend frontEnd = new ServerFrontend();
	}

	private void addWidgets() {
		start = new JButton("Start server");
		stop = new JButton("Stop server");
		
		start.setName("start");
		stop.setName("stop");
		
		start.addActionListener(this);
		stop.addActionListener(this);

		panel.add(start);
		panel.add(stop);
	}
	
	private void startServer() {
		server.startServer();
	}
	
	private void stopServer() {
		server.stopServer();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object o = arg0.getSource();
		if( ((JButton)o).getName().equals("start")) {
			startServer();
		}
		else if(((JButton)o).getName().equals("stop")) {
			stopServer();
		}
	}
}
