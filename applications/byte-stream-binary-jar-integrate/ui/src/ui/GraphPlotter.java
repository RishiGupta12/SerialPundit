/*
 * This file is part of SerialPundit.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class GraphPlotter {

	private int data;
	private String dataStr;
	private byte[] dataByte = new byte[1];
	private final InputStream in;
	//	private final OutputStream out;
	private final ICleanUpListener cleanupui;
	private JFrame mainFrame;
	private DataReaderPanel dataReader;
	private Thread dataReaderThread;
	private Graphics2D g2d;
	private JTextField userText = new JTextField("started");
	private AtomicBoolean exit = new AtomicBoolean(false);

	private class DataReaderPanel extends JPanel implements Runnable {

		private static final long serialVersionUID = -8580754961552006629L;

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g2d = (Graphics2D) g;

			// clear screen
			g2d.drawRect(0, 0, 300, 300);
			g2d.setColor(Color.BLACK);
			g2d.fillRect(0, 0, 300, 300);

			// draw x/y-axis
			g2d.setColor(Color.WHITE);
			g2d.drawLine(25, 40, 25, 140);
			g2d.drawLine(25, 140, 225, 140);

			// draw data value
			if((data & 0x80) == 0x80) {
				g2d.drawLine(25, 100, 45, 100);
			}else {
				g2d.drawLine(25, 130, 45, 130);
			}

			if((data & 0x40) == 0x40) {
				g2d.drawLine(45, 100, 65, 100);
			}else {
				g2d.drawLine(45, 130, 65, 130);
			}

			if((data & 0x20) == 0x20) {
				g2d.drawLine(65, 100, 85, 100);
			}else {
				g2d.drawLine(65, 130, 85, 130);
			}

			if((data & 0x10) == 0x10) {
				g2d.drawLine(85, 100, 105, 100);
			}else {
				g2d.drawLine(85, 130, 105, 130);
			}

			if((data & 0x08) == 0x08) {
				g2d.drawLine(105, 100, 125, 100);
			}else {
				g2d.drawLine(105, 130, 125, 130);
			}

			if((data & 0x04) == 0x04) {
				g2d.drawLine(125, 100, 145, 100);
			}else {
				g2d.drawLine(125, 130, 145, 130);
			}

			if((data & 0x02) == 0x02) {
				g2d.drawLine(145, 100,165, 100);
			}else {
				g2d.drawLine(145, 130, 165, 130);
			}

			if((data & 0x01) == 0x01) {
				g2d.drawLine(165, 100, 185, 100);
			}else {
				g2d.drawLine(165, 130, 185, 130);
			}
		}

		@Override
		public void run() {
			while(exit.get() == false) {
				try {
					data = in.read();
					if(data != -1) {
						dataByte[0] = (byte)(data & 0xFF);
						dataStr = new String(dataByte);
						userText.setText(dataStr);
						data = Character.getNumericValue((data & 0xFF));
						this.repaint();
					}
				} catch (Exception e) {
					if(data == -1) {
						// do nothing, just stream has been closed.
					}else {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public GraphPlotter(InputStream in, OutputStream out, ICleanUpListener cleanup) {
		this.in = in;
		//		this.out = out;
		this.cleanupui = cleanup;

		mainFrame = new JFrame("Plotter");
		mainFrame.setSize(300, 390);
		mainFrame.setLayout(new FlowLayout());
		mainFrame.setBackground(Color.BLACK);
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent){
				cleanupui.onAppExit();
				mainFrame.setVisible(false);
				mainFrame.dispose();
				System.exit(0);
			}       
		});

		dataReader = new DataReaderPanel();
		dataReader.setPreferredSize(new Dimension(300, 300));
		mainFrame.add(dataReader);
		//userText.setPreferredSize(new Dimension(100, 100));
		mainFrame.add(userText);

		dataReaderThread = new Thread(dataReader);
		dataReaderThread.start();
		mainFrame.setVisible(true);
	}
}
