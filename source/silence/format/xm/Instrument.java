/* Instrument.java - Stores information about an instrument
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

/**
 * This class stores information about an instrument
 * @author Fredrik Ehnbom
 * @version $Id: Instrument.java,v 1.1 2000/06/07 13:28:15 quarn Exp $
 */
public class Instrument {

	Sample sample[];
	int samples = 0;

	public Instrument(BufferedInputStream in) throws IOException {

		// Instrument size
		byte b[] = new byte[4];
		in.read(b);
		int size = (int) (b[0]) - 7;

		// Instrument name
		b = new byte[22];
		in.read(b);
		System.out.println("Name: " + new String(b));

		// Instrument type (always 0)
		// Note: not always 0 but it says so in the documents...
		int type = in.read();

		// Number of samples in instrument
		b = new byte[2];
		in.read(b);
		samples = b[0];

		// Seems like there is four extra bytes to read here
		if (size > 0) {
			for (int i = 0; i < 4; i++) in.read();
		}

		if (samples > 0) {
			// Sample header size
			b = new byte[4];
			in.read(b);

			// Sample number for all notes
			b = new byte[96];
			in.read(b);

			// Points for volume envelope
			b = new byte[48];
			in.read(b);

			// Points for panning envelope
			b = new byte[48];
			in.read(b);

			// Number of volume points
			in.read();

			// Number of panning points 
			in.read();

			// Volume sustain point
			in.read();

			// Volume loop start point
			in.read();

			// Volume loop end point
			in.read();

			// Panning sustain point
			in.read();

			// Panning loop start point
			in.read();

			// Panning loop end point
			in.read();

			// Volume type: bit 0: on; 1: Sustain; 2: Loop
			in.read();

			// Panning type: bit 0: on; 1: Sustain; 2: Loop
			in.read();

			// Vibrato type
			in.read();

			// Vibrato sweep
			in.read();

			// Vibrato depth
			in.read();

			// Vibrator rate
			in.read();

			// Volume fadeout
			b = new byte[2];
			in.read(b);

			// reserved
			b = new byte[22];
			in.read(b);

			sample = new Sample[samples];

			for (int i = 0; i < samples; i++) {
				sample[i] = new Sample(in);
			}
			for (int i = 0; i < samples; i++) {
				sample[i].readData(in);
			}
		}
	}
}
/*
 * ChangeLog:
 * $Log: Instrument.java,v $
 * Revision 1.1  2000/06/07 13:28:15  quarn
 * files for the xm sound format
 *
 */
