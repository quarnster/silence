/* Sample.java - Stores information about a sample
 * Copyright (C) 2000-2002 Fredrik Ehnbom
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
 * @version $Id: Sample.java,v 1.9 2003/09/01 09:06:50 fredde Exp $
 */
class Sample {
	int sampleLength = 0;
	private int sampleQuality = 0;

	int loopType = 0;
	int loopStart = 0;
	int loopEnd = 0;

	byte relativeNote = 0;
	byte fineTune = 0;
	int volume;

	short[] sampleData;


	public Sample(BufferedInputStream in)
		throws IOException
	{
		// Sample length
		sampleLength = Xm.make32Bit(Xm.read(in, 4));

		// Sample loop start
		loopStart = Xm.make32Bit(Xm.read(in, 4));

		// Sample loop length
		loopEnd = Xm.make32Bit(Xm.read(in, 4));

		if (loopStart + loopEnd > sampleLength)
			loopEnd = (sampleLength) - loopStart;

		// Volume
		volume = in.read();

		// Finetune (signend byte -128...+127)
		fineTune = (byte) in.read();

		// Type: Bit 0-1: 0 = No loop,
		//                1 = Forward loop,
		//		  2 = Ping-pong loop;
		//                4: 16-bit sampledata
		loopType = in.read();
		sampleQuality = ((int) loopType & 0x10) != 0 ? 16 : 8;

		if ((loopType & 0x3) == 0) {
			// no looping
			loopStart = 0;
			loopEnd = sampleLength;
		}

		// Panning (0-255)
		in.read();

		// Relative note number (signed byte)
		relativeNote = (byte) in.read();

		// Reserved
		in.read();

		// Sample name
		Xm.read(in, 22);
	}

	public void readData(BufferedInputStream in)
		throws IOException
	{
		if (sampleQuality == 16) {
			sampleLength >>= 1;
			loopEnd >>= 1;
			loopStart >>= 1;

			byte[] temp = Xm.read(in, 2 * sampleLength);
			sampleData = new short[sampleLength+4];

			int tmpPos = 0;

			int samp = 0;

			for (int i = 0; i < sampleLength; i++, tmpPos += 2) {
				samp += Xm.make16Bit(temp, tmpPos);
				sampleData[i] = (short) (samp);
			}
		} else {
			sampleData = new short[sampleLength+4];
			byte[] temp = Xm.read(in, sampleLength);
			int samp = 0;

			for (int i = 0; i < sampleLength; i++) {
				samp += temp[i]&0xff;
				sampleData[i] = (short) (samp << 8);
			}
		}

		int pos2 = 0;

		if ((loopType & 0x2) == 0) {
			if ((loopType & 0x1) != 0)
				pos2 = loopStart;

			for (int i = 0; i < 3; i++) 
				sampleData[sampleLength+1+i] = sampleData[pos2+i];

		} else if ((loopType & 0x2) != 0) {
			pos2 = sampleLength;

			for (int i = 0; i < 3; i++) 
				sampleData[sampleLength+1+i] = sampleData[pos2-i];

		}
		System.arraycopy(sampleData, 0, sampleData, 1, sampleData.length - 3);

		if ((loopType & 0x1) != 0) sampleData[0] = sampleData[loopStart];
		else if ((loopType & 0x2) != 0) sampleData[0] = sampleData[1];

		loopStart <<= 10;
		loopEnd <<= 10;
	}
}
/*
 * ChangeLog:
 * $Log: Sample.java,v $
 * Revision 1.9  2003/09/01 09:06:50  fredde
 * cubic spline
 *
 * Revision 1.8  2003/08/21 09:22:21  fredde
 * loopfixes
 *
 * Revision 1.7  2002/03/20 13:30:04  fredde
 * sampleData is now an array of shorts to assure 16-bit sound quality
 *
 * Revision 1.6  2000/12/21 17:21:14  fredde
 * relativeNote, fineTune -> byte
 *
 * Revision 1.5  2000/10/14 19:11:35  fredde
 * made sampleData to an byte array
 * now uses Xm.make[16|32]Bit()
 * 16-bit samples working!
 *
 * Revision 1.4  2000/10/12 15:06:28  fredde
 * made sampleData to an int[], 16-bit samples works
 * better but still not good.
 *
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


