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
package silence.format.xm;

import java.io.*;

/**
 * Stores sample data
 * @author Fredrik Ehnbom
 * @version $Id: Sample.java,v 1.2 2000/06/08 16:29:30 quarn Exp $
 */
class Sample {

	private int samplelength = 0;
	private int looptype = 0;
	private int samplequality = 0;
	private int relativenote = 0;
	private int finetune = 0;
	protected byte[] sampleData;


	public Sample(BufferedInputStream in) throws IOException {
		// Sample length
		byte b[] = new byte[4];
		in.read(b);

		int t[] = new int[4];
		t[0] = (int) ((b[0] < 0 ) ? 256 + b[0] : b[0]);
		t[1] = (int) ((b[1] < 0 ) ? 256 + b[1] : b[1]);
		t[2] = (int) ((b[2] < 0 ) ? 256 + b[2] : b[2]);
		t[3] = (int) ((b[3] < 0 ) ? 256 + b[3] : b[3]);
		samplelength = (t[0] << 0) + (t[1] << 8) + (t[2] << 16) + (t[3] << 24);

		// Sample loop start
		b = new byte[4];
		in.read(b);

		// Sample loop length
		b = new byte[4];
		in.read(b);

		// Volume
		in.read();

		// Finetune (signend byte -128...+127)
		finetune = in.read();

		// Type: Bit 0-1: 0 = No loop,
		//                1 = Forward loop,
		//		  2 = Ping-pong loop;
		//                4: 16-bit sampledata
		looptype = in.read();
		samplequality = Pattern.isSet((int) looptype, 4) ? 16 : 8;

		// Panning (0-255)
		in.read();

		// Relative note number (signed byte)
		relativenote = in.read();

		// Reserved
		in.read();

		// Sample name
		b = new byte[22];
		in.read(b);
	}

	public void readData(BufferedInputStream in) throws IOException {
		if (samplequality == 16) {
			samplelength >>= 1;
			sampleData = new byte[2 * samplelength];
			in.read(sampleData);
		} else {
			sampleData = new byte[samplelength];
			in.read(sampleData);
		}

		int p = 0;
		int old = 0;

		for(int i = 0; i < sampleData.length; i++) {
			p = ((int) sampleData[i]) + old;
			sampleData[i] = (byte) p;
			old = p;
		}
	}
}
