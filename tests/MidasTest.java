/* MidasTest.java - An example player for the midas device
 * Copyright (C) 2000 Fredrik Ehnbom
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

import silence.*;
import silence.devices.*;
import silence.devices.midas.*;

import java.awt.*;
import java.awt.event.*;

/**
 * An example player for the Midas device
 * @author Fredrik Ehnbom
 * @version $Id: MidasTest.java,v 1.1 2000/04/30 13:16:17 quarn Exp $
 */
public class MidasTest extends Frame {

	/**
	 * The AudioDevice to use for playing the AudioFormat 
	 */
	private AudioDevice audioDevice = new MidasDevice() {
		// This is how you redefine the sync method.
		public void sync(int eff) {
			System.out.println("Midas wants to sync... (effect num: " + eff + ")");
		}
	};

	private String file = null;

	/**
	 * Creates a new Silence player.
	 * @param file The file to play
	 */
	public MidasTest(String file) {
		super("Midas test: " + file);
		this.file = file;
		setLayout(new BorderLayout());

		try {
			audioDevice.init(true);
		} catch (AudioException me) {
			me.printStackTrace();
			audioDevice.close();
			System.exit(1);
		}

		Button b = new Button("Play");
		b.addActionListener(listener);
		add("West", b);

		b = new Button("Stop");
		b.addActionListener(listener);
		add("East", b);

		b = new Button("Pause");
		b.addActionListener(listener);
		add("South", b);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				audioDevice.stop();
				audioDevice.close();
				dispose();
				System.exit(0);
			}
		});
		setSize(320, 240);
		show();
	}

	ActionListener listener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String event = ((Button) e.getSource()).getLabel();

			if (event.equals("Play")) {
				try {
					audioDevice.play(file, false);
				} catch (AudioException ae) {
					ae.printStackTrace();
				}
			} else if (event.equals("Stop")) {
				audioDevice.stop();
			}
		}
	};

	public static void main(String args[]) {
		if (args.length != 1) {
			System.out.println("Usage: java MidasTest <song>");
		} else {
			new MidasTest(args[0]);
		}
	}
}
/*
 * ChangeLog:
 * $Log: MidasTest.java,v $
 * Revision 1.1  2000/04/30 13:16:17  quarn
 * A little test for the Midas device
 *
 */
