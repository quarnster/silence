/* Sample.java - Stores information about a sample
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
 * Stores sample data
 *
 * @author Fredrik Ehnbom
 * @version $Id: Sample.java,v 1.3 2000/10/07 13:52:46 fredde Exp $
 */
class Sample {
	private int sampleLength = 0;
	private int sampleQuality = 0;

	int loopType = 0;
	int loopStart = 0;
	int loopEnd = 0;

	int relativeNote = 0;
	int fineTune = 0;
	byte[] sampleData;
	int	volume;


	public Sample(BufferedInputStream in)
		throws IOException
	{
		// Sample length
		byte b[] = Xm.read(in, 4);

		int t[] = new int[4];
		t[0] = (int) ((b[0] < 0 ) ? 256 + b[0] : b[0]);
		t[1] = (int) ((b[1] < 0 ) ? 256 + b[1] : b[1]);
		t[2] = (int) ((b[2] < 0 ) ? 256 + b[2] : b[2]);
		t[3] = (int) ((b[3] < 0 ) ? 256 + b[3] : b[3]);
		sampleLength = (t[0] << 0) + (t[1] << 8) + (t[2] << 16) + (t[3] << 24);

		// Sample loop start
		b = Xm.read(in, 4);
		t = new int[4];
		t[0] = (int) ((b[0] < 0 ) ? 256 + b[0] : b[0]);
		t[1] = (int) ((b[1] < 0 ) ? 256 + b[1] : b[1]);
		t[2] = (int) ((b[2] < 0 ) ? 256 + b[2] : b[2]);
		t[3] = (int) ((b[3] < 0 ) ? 256 + b[3] : b[3]);
		loopStart = (t[0] << 0) + (t[1] << 8) + (t[2] << 16) + (t[3] << 24);

		// Sample loop length
		b = Xm.read(in, 4);
		t = new int[4];
		t[0] = (int) ((b[0] < 0 ) ? 256 + b[0] : b[0]);
		t[1] = (int) ((b[1] < 0 ) ? 256 + b[1] : b[1]);
		t[2] = (int) ((b[2] < 0 ) ? 256 + b[2] : b[2]);
		t[3] = (int) ((b[3] < 0 ) ? 256 + b[3] : b[3]);
		loopEnd = (t[0] << 0) + (t[1] << 8) + (t[2] << 16) + (t[3] << 24);

		// Volume
		volume = in.read();

		// Finetune (signend byte -128...+127)
		fineTune = in.read();
		if (fineTune > 127) fineTune -= 256;
		System.out.println("fineTune: " + fineTune);

		// Type: Bit 0-1: 0 = No loop,
		//                1 = Forward loop,
		//		  2 = Ping-pong loop;
		//                4: 16-bit sampledata
		loopType = in.read();
		sampleQuality = ((int) loopType & 0x10) != 0 ? 16 : 8;
		System.out.println("looptype: " + ((loopType & 0x1) != 0 ? "forward loop" : (loopType & 0x2) != 0 ? "pingpong" : "noloop"));

		// Panning (0-255)
		in.read();

		// Relative note number (signed byte)
		relativeNote = in.read();
		if (relativeNote > 95) relativeNote -= 256;
		System.out.println("relativeNote: " + relativeNote);

		// Reserved
		in.read();

		// Sample name
		b = Xm.read(in, 22);
	}

	public void readData(BufferedInputStream in)
		throws IOException
	{
		if (sampleQuality == 16) {
			// TODO: fix...
			sampleLength >>= 1;
			byte[] temp = Xm.read(in, 2 * sampleLength);
			sampleData = new byte[sampleLength];

			for (int i = 0; i < temp.length - 1; i++) {
				byte tmp = temp[i];
				temp[i] = temp[i + 1];
				temp[i + 1] = tmp;
			}

/*
			int p = 0;
			int old = 0;

			for (int i = 0; i < temp.length; i++) {
				p = ((int) temp[i]) + old;
				temp[i] = (byte) p;
				old = p;
			}
*/

			int tmpPos = 0;
			for (int i = 0; i < sampleData.length; i++) {
				sampleData[i] = (byte) (temp[tmpPos++] + temp[tmpPos++] >> 8);
			}

			int p = 0;
			int old = 0;

			for (int i = 0; i < sampleData.length; i++) {
				p = sampleData[i] + old;
				sampleData[i] = (byte) p;
				old = p;
			}

		} else {
			sampleData = Xm.read(in, sampleLength);

			int p = 0;
			int old = 0;

			for (int i = 0; i < sampleData.length; i++) {
				p = ((int) sampleData[i]) + old;
				sampleData[i] = (byte) p;
				old = p;
			}

		}
	}
}
/*
 * ChangeLog:
 * $Log: Sample.java,v $
 * Revision 1.3  2000/10/07 13:52:46  fredde
 * Fixed to read in correctly.
 * Finetunes and relativenotes are working now.
 *
 * Revision 1.2  2000/10/01 17:08:50  fredde
 * no longer uses Pattern.isSet()
 *
 * Revision 1.1.1.1  2000/09/25 16:34:33  fredde
 * initial commit
 *
 */
