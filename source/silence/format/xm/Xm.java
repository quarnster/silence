/* Xm.java - The general xm class
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
package silence.format.xm;

import java.io.*;
import java.net.URL;

/**
 * The general xm class
 * @author Fredrik Ehnbom
 * @version $Id: Xm.java,v 1.1 2000/06/07 13:28:15 quarn Exp $
 */
public class Xm {

	private String title = "";
	private String tracker = "";
	private int songlength = 0;
	private int patnum = 0;
	private int patorder[];
	private int instrnum = 0;
	private int channels = 0;

	public Xm(InputStream is) throws IOException {

		BufferedInputStream in = new BufferedInputStream(is);

		readGeneralInfo(in);

		for (int i = 0; i < patnum; i++) {
			new Pattern(channels, in);
		}

		for (int i = 0; i < instrnum; i++) {
			new Instrument(in);
		}
		in.close();
	}

	private void readGeneralInfo(BufferedInputStream in) throws IOException {
		// "Extended Module: "
		byte b[] = new byte[17];
		in.read(b);

		if (!(new String(b)).equalsIgnoreCase("Extended Module: ")) {
			throw new IOException("This is not a xm file!");
		}


		// Module name
		b = new byte[20];
		in.read(b);

		title = new String(b);
		System.out.println("Title: " + title);

		in.read();

		// Tracker name
		b = new byte[20];
		in.read(b);

		tracker = new String(b);
		System.out.println("Tracker: " + tracker);

		System.out.println("Version: 1: " + in.read() + ", 2: " + in.read());

		// Header size
		b = new byte[4];
		in.read(b);

		// Song length (in pattern order table)
		b = new byte[2];
		in.read(b);
		System.out.println("song length: " +  b[0]);

		songlength = (int) b[0];


		// Restart position
		b = new byte[2];
		in.read(b);
		System.out.println("Restart position: " + b[0]);


		// Number of channels (2,4,6,8,10,...,32)
		b = new byte[2];
		in.read(b);
		System.out.println("Number of channels: " + b[0]);
		channels = b[0];


		// Number of patterns (max 128)
		b = new byte[2];
		in.read(b);
		System.out.println("Number of patterns: " + b[0]);
		patnum = (int) b[0];

		// Number of instruments (max 128)
		b = new byte[2];
		in.read(b);
		instrnum = (int) ((b[0] < 0) ? 256 + b[0] : b[0]);
		System.out.println("Number of instruments: " + instrnum);

		// Flags: bit 0: 0 = Amiga frequency table;
		//               1 = Linear frequency table
		b = new byte[2];
		in.read(b);

		// Default tempo
		b = new byte[2];
		in.read(b);
		System.out.println("Default tempo: " + b[0]);

		// Default BPM
		b = new byte[2];
		in.read(b);
		System.out.println("Default BPM: " + (int) ((b[0] < 0) ? 256 + b[0] : b[0]));

		// Pattern order table
		b = new byte[256];
		in.read(b);
		patorder = new int[songlength];

		System.out.println("Pattern order table: ");
		for (int i = 0; i < songlength; i++) {
			System.out.print(b[i] + ", ");

			patorder[i] = b[i];
		}
		System.out.println();

	}

	public static void main(String args[]) {

		try {
			URL u = new URL(args[0]);
			Xm xm = new Xm(u.openStream());
		} catch (IOException ioe) {
			System.err.println(ioe);
		}
	}
}
/*
 * ChangeLog:
 * $Log: Xm.java,v $
 * Revision 1.1  2000/06/07 13:28:15  quarn
 * files for the xm sound format
 *
 */
