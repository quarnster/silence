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
 * @version $Id: MidasTest.java,v 1.7 2000/06/26 16:09:43 quarn Exp $
 */
public class MidasTest extends Frame implements CallbackClass {

	/**
	 * The AudioDevice to use for playing the AudioFormat 
	 */
	private AudioDevice audioDevice = null;

	private Scrollbar vol = new Scrollbar(Scrollbar.HORIZONTAL, 100, 4, 0, 100);
	private String file = null;

	/**
	 * The callback for when the device finds a sync callback
	 */
	public void syncCallback(int effect) {
		System.out.println(audioDevice.getName() + " sync: " + effect);
	}

	/**
	 * Creates a new Silence player.
	 * @param file The file to play
	 */
	public MidasTest(String file) {
		super("Midas test: " + file);
		this.file = file;
		setLayout(new BorderLayout());

		try {
			// load the device
			audioDevice = new Silence().loadDevice(Silence.MidasDevice);
		} catch (AudioException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// set callback class
		audioDevice.setCallbackClass(this);

		vol.addAdjustmentListener(volListener);
		add("North", vol);

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

	AdjustmentListener volListener = new AdjustmentListener() {
		public void adjustmentValueChanged(AdjustmentEvent e) {
			audioDevice.setVolume(vol.getValue());
		}
	};

	ActionListener listener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String event = ((Button) e.getSource()).getLabel();

			if (event.equals("Play")) {
				try {
					audioDevice.play(file, false);
					audioDevice.setVolume(vol.getValue());
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
 * Revision 1.7  2000/06/26 16:09:43  quarn
 * the volume system now leaps from 0 to 100 (as in percent)
 *
 * Revision 1.6  2000/06/25 18:34:11  quarn
 * updated for the new CallbackClass
 *
 * Revision 1.5  2000/06/25 15:52:30  quarn
 * updated
 *
 * Revision 1.4  2000/06/20 22:32:29  quarn
 * no need to load the libs
 *
 * Revision 1.3  2000/06/10 18:05:33  quarn
 * fixed
 *
 * Revision 1.2  2000/05/07 09:32:59  quarn
 * Added the setVolume function
 *
 * Revision 1.1  2000/04/30 13:16:17  quarn
 * A little test for the Midas device
 *
 */
