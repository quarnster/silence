/* Pattern.java - Stores pattern data
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
package org.gjt.fredde.silence.format.xm;

import java.io.*;

/**
 * Stores pattern data
 *
 * @author Fredrik Ehnbom
 * @version $Id: Pattern.java,v 1.2 2000/10/01 17:07:22 fredde Exp $
 */
class Pattern {
	private int channels = 0;
	private int nrows = 0;
	protected byte[] data;

	public Pattern(int channels, BufferedInputStream in)
		throws IOException
	{
		this.channels = channels;

		// Pattern header length
		byte b[] = new byte[4];
		int ih[];
		in.read(b);

		// Packing type (always 0)
		in.read();

		// Number of rows in pattern (1...256)
		b = new byte[2];
		ih = new int[2];
		in.read(b);
		ih[0] = (int) ((b[0] < 0 ) ? 256 + b[0] : b[0]);
		ih[1] = (int) ((b[1] < 0 ) ? 256 + b[1] : b[1]);
		nrows = (ih[0] << 0) + (ih[1] << 8);

		// Packed patterndata size
		b = new byte[2];
		in.read(b);
		ih[0] = (int) ((b[0] < 0 ) ? 256 + b[0] : b[0]);
		ih[1] = (int) ((b[1] < 0 ) ? 256 + b[1] : b[1]);
		int size = (ih[0] << 0) + (ih[1] << 8);

		data = new byte[size];
		if (size == 0) {
			return;
		}

		int read = in.read(data);
		while (read != data.length) {
			read += in.read(data, read, data.length - read);
		}
	}

	public static final String notes[] = {
		"???",
		"C-0", "C#0", "D-0", "D#0", "E-0", "F-0", "F#0", "G-0", "G#0", "A-0", "A#0", "B-0",
		"C-1", "C#1", "D-1", "D#1", "E-1", "F-1", "F#1", "G-1", "G#1", "A-1", "A#1", "B-1",
		"C-2", "C#2", "D-2", "D#2", "E-2", "F-2", "F#2", "G-2", "G#2", "A-2", "A#2", "B-2",
		"C-3", "C#3", "D-3", "D#3", "E-3", "F-3", "F#3", "G-3", "G#3", "A-3", "A#3", "B-3",
		"C-4", "C#4", "D-4", "D#4", "E-4", "F-4", "F#4", "G-4", "G#4", "A-4", "A#4", "B-4",
		"C-5", "C#5", "D-5", "D#5", "E-5", "F-5", "F#5", "G-5", "G#5", "A-5", "A#5", "B-5",
		"C-6", "C#6", "D-6", "D#6", "E-6", "F-6", "F#6", "G-6", "G#6", "A-6", "A#6", "B-6",
		"C-7", "C#7", "D-7", "D#7", "E-7", "F-7", "F#7", "G-7", "G#7", "A-7", "A#7", "B-7",
		"[-]"
	};
}
/*
 * ChangeLog:
 * $Log: Pattern.java,v $
 * Revision 1.2  2000/10/01 17:07:22  fredde
 * removed unused stuff
 *
 * Revision 1.1.1.1  2000/09/25 16:34:33  fredde
 * initial commit
 *
 */
