/* SimplePlayTest.java - A simple example player for silence
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

import java.awt.*;
import java.awt.event.*;

import org.gjt.fredde.silence.*;
import org.gjt.fredde.silence.format.*;


/**
 * A simple example player for silence.
 *
 * @author Fredrik Ehnbom
 * @version $Id: SimplePlayTest.java,v 1.1 2000/09/29 19:32:11 fredde Exp $
 */
public class SimplePlayTest
	extends Frame
	implements WindowListener, Runnable
{

	private String file;
	private Silence silence = new Silence();

	public static void main(String args[]) {
		if (args.length != 1) {
			System.out.println("usage: java SimplePlayTest <music file>");
			System.exit(1);
		} else {
			SimplePlayTest test = new SimplePlayTest(args[0]);
		}
	}

	public SimplePlayTest(String file) {
		super("a simple test...");
		this.file = file;

		addWindowListener(this);

		Thread t = new Thread(this);
		t.start();

		setSize(320, 240);
		show();
	}

	public void run() {
		try {
			// initialize silence
			silence.init();

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
/*
 * ChangeLog:
 * $Log: SimplePlayTest.java,v $
 * Revision 1.1  2000/09/29 19:32:11  fredde
 * A simple play test
 *
 */
