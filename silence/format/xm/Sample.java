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
 * @version $Id: Sample.java,v 1.6 2000/12/21 17:21:14 fredde Exp $
 */
class Sample {
	private int sampleLength = 0;
	private int sampleQuality = 0;

	int loopType = 0;
	int loopStart = 0;
	int loopEnd = 0;

	byte relativeNote = 0;
	byte fineTune = 0;
	byte[] sampleData;
	int	volume;


	public Sample(BufferedInputStream in)
		throws IOException
	{
		// Sample length
		sampleLength = Xm.make32Bit(Xm.read(in, 4));

		// Sample loop start
		loopStart = Xm.make32Bit(Xm.read(in, 4));

		// Sample loop length
		loopEnd = Xm.make32Bit(Xm.read(in, 4));

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
			sampleData = new byte[sampleLength];

			int tmpPos = 0;

			int samp = 0;

			for (int i = 0; i < sampleData.length; i++, tmpPos += 2) {
				samp += Xm.make16Bit(temp, tmpPos);
				sampleData[i] = (byte) (samp >> 8);
			}
		} else {
			sampleData = Xm.read(in, sampleLength);
			int samp = 0;

			for (int i = 0; i < sampleData.length; i++) {
				samp += sampleData[i]&0xff;
				sampleData[i] = (byte) samp;
			}

		}
	}
}
/*
 * ChangeLog:
 * $Log: Sample.java,v $
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
