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
package silence.format.xm;

import java.io.*;

/**
 * Stores pattern data
 * @author Fredrik Ehnbom
 * @version $Id: Pattern.java,v 1.2 2000/06/08 16:29:30 quarn Exp $
 */
class Pattern {

	private int channels = 0;
	private int nrows = 0;

	public Pattern(int channels, BufferedInputStream in) throws IOException {
		this.channels = channels;

		// Pattern header length
		byte b[] = new byte[4];
		in.read(b);

		// Packing type (always 0)
		in.read();

		// Number of rows in pattern (1...256)
		b = new byte[2];
		in.read(b);
		nrows = (int) ((b[0] < 0) ? 256 + b[0] : b[0]);

		// Packed patterndata size
		b = new byte[2];
		in.read(b);
		int size = (int) ((b[0] < 0) ? 256 + b[0] : b[0]);

		if (size == 0) {
			return;
		}

		// print the not info in colums like ft2 does.
/*
		System.out.print("    ");

		for (int i = 1; i <= channels; i++) {
			if (i < 10) {
				System.out.print("   --0" + i + "--     ** ");
			} else {
				System.out.print("   --" + i + "--     ** ");
			}
		}
*/


		// Packed pattern data
		for (int i = 0; i < nrows; i++) {
			// print the not info in colums like ft2 does.
/*
			if (i < 10) {
				System.out.print("\n00" + i + " ");
			} else if (i < 100) {
				System.out.print("\n0" + i + " ");
			} else {
				System.out.print("\n" + i + " ");
			}
*/


			for (int j = 0; j < channels; j++) {
//				printNote(in);
				readNote(in);
			}
		}
	}

	/**
	 * Checks if the bit "bit" is set in byte "b"
	 * @param b The byte
	 * @param bit The bit to check
	 */
	public static boolean isSet(int b, int bit) {
		// the bits are in order 7 -> 0
		bit = 7 - bit;
		int mask = 0x080;
		mask >>= bit;

		return ( (( b & mask) != 0) ? true : false);
	}

	private void readNote(BufferedInputStream in) throws IOException {
		int i = in.read();


		if (isSet(i, 7)) {
			// note follows
			if (isSet(i, 0)) {
				in.read();
			}

			// instrument follows
			if (isSet(i, 1)) {
				in.read();
			}

			// volume follows
			if (isSet(i, 2)) {
				in.read();
			}

			// effect follows
			if (isSet(i, 3)) {
				in.read();
			}

			// effect parameter follows
			if (isSet(i, 4)) {
				in.read();
			}
		} else {
			in.read();
			in.read();
			in.read();
			in.read();
		}
	}

	private void printNote(BufferedInputStream in) throws IOException {
		int b  = in.read();

		if (isSet(b, 7)) {
			if (isSet(b, 0)) {
				System.out.print(translateNote(in.read()));
			} else {
				System.out.print("---");
			}

			if (isSet(b, 1)) {
				String str = Integer.toHexString(in.read());

				if (str.length() == 2) {
					System.out.print(" " + str);
				} else {
					System.out.print("  " + str);
				}
			} else {
				System.out.print(" --");
			}

			if (isSet(b, 2)) {
				int t = in.read();

				if ( t < 0 ) {
					t = 256 - t;
				}

				if (t >= 10 && t <= 50) {
					System.out.print(" " + t);
				} else if (t >= 192 && t <= 208) {
					System.out.print(" p" + Integer.toHexString((t - 192)));
				} else {
					System.out.print(" " + t);
				}
			} else {
				System.out.print(" --");
			}

			if (isSet(b, 3)) {
				System.out.print(" " + Integer.toHexString(in.read()));
			} else {
				System.out.print(" 0");
			}

			if (isSet(b, 4)) {
				String str = "" + in.read();

				if (str.length() == 2) {
					System.out.print(str + " ** ");
				} else {
					System.out.print(str + "0 ** ");
				}
			} else {
				System.out.print("00 ** ");
			}
		} else {
			System.out.print(translateNote(b));
			System.out.print(" " + in.read());
			String str = Integer.toHexString(in.read());
			if (str.length() == 2) {
				System.out.print(" " + str);
			} else {
				System.out.print("  " + str);
			}
			System.out.print(" " + in.read());
			str = "" + in.read();
			if (str.length() == 2) {
				System.out.print(str + " ** ");
			} else {
				System.out.print(str + "0 ** ");
			}
		}
	}

	final String notes[] = {
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

	private String translateNote(int i) {
		if (i < 0) {
			return "err";
		} else {
			return notes[i];
		}
	}
}
/*
 * ChangeLog:
 * $Log: Pattern.java,v $
 * Revision 1.2  2000/06/08 16:29:30  quarn
 * fixed/updated/etc...
 *
 * Revision 1.1  2000/06/07 13:28:15  quarn
 * files for the xm sound format
 *
 */
