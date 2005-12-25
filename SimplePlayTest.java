/* SimplePlayTest.java - A simple example player for silence
 * Copyright (C) 2000-2005 Fredrik Ehnbom
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

import java.awt.*;
import java.awt.event.*;

import silence.*;
import silence.format.*;


/**
 * A simple example player for silence.
 *
 * @author Fredrik Ehnbom
 */
public class SimplePlayTest
	extends Frame
	implements WindowListener
{
	private Silence silence = new Silence();

	public static void main(String args[]) {
		if (args.length < 1) {
			System.out.println("usage: java SimplePlayTest <music file> (<khz>)");
			System.out.println("(<khz>: 44, 22, 11)");
			System.exit(1);
		} else if (args.length == 1) {
			SimplePlayTest test = new SimplePlayTest(args[0], "44");	
		} else {
			SimplePlayTest test = new SimplePlayTest(args[0], args[1]);
		}
	}

	public SimplePlayTest(String file, String khz) {
		super("a simple test...");
		addWindowListener(this);
		setSize(320, 240);
		show();

		try {
			int dform = 0;
			if (khz.equals("44")) {
				dform = Silence.FORMAT_PCM44K16S;
			} else if (khz.equals("22")) {
				dform = Silence.FORMAT_PCM22K16S;
			} else if (khz.equals("11")) {
				dform = Silence.FORMAT_PCM11K16S;
			} else {
				System.err.println("<khz>: 44, 22, 11");
				System.exit(1);
			}
			// initialize silence
			silence.init(dform);

			// load the specified file
			AudioFormat format = silence.load(file);

			// play the file
			silence.play(format);
		} catch (AudioException ae) {
			ae.printStackTrace();
			System.exit(1);
		}

	}

	public void windowClosing(WindowEvent e) {
		silence.stop();
		dispose();
		System.exit(0);
	}

	public void windowClosed(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
}
