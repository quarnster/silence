/* Au.java - Loading and playing .au-files
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
package org.gjt.fredde.silence.format.au;

import java.io.*;

import org.gjt.fredde.silence.format.*;

import org.komplex.audio.*;

/**
 * A class for loading and playing .au-files.
 * Information is taken from au.txt which you
 * can find at http://www.wotsit.org
 * 
 * @author Fredrik Ehnbom
 * @version $Id: Au.java,v 1.3 2000/12/21 17:16:00 fredde Exp $
 */
public class Au
	extends AudioFormat
{

	private int samplerate = 0;
	private int[] sampledata;
	private double samppos = 0;
	private double pitch = 0;

	public Au() {
	}

	/**
	 * Load the file into memory
	 * @param in The InputStream to read the file from
	 */
	public void load(BufferedInputStream in)
		throws IOException
	{
		// magic number	the value 0x2e736e64 (ASCII ".snd")
		System.out.println("0x2e: " + ((in.read() == 0x2e) ? "yes" : "no"));
		System.out.println("0x73: " + ((in.read() == 0x73) ? "yes" : "no"));
		System.out.println("0x6e: " + ((in.read() == 0x6e) ? "yes" : "no"));
		System.out.println("0x64: " + ((in.read() == 0x64) ? "yes" : "no"));

		// data offset	the offset, in octets, to the data part.
		//			The minimum valid number is 24 (decimal).
		int offset = (in.read() << 24) | (in.read() << 16) | (in.read() << 8) | (in.read());
		System.out.println("offset: " + offset);

		// data size		the size in octets, of the data part.
		//			If unknown, the value 0xffffffff should
		//			be used.
		int size = (in.read() << 24) | (in.read() << 16) | (in.read() << 8) | (in.read());
		System.out.println("size: " + size);

		// encoding		the data encoding format:
		//
		//			value		format
		//			   1		8-bit ISDN u-law
		//			   2		8-bit linear PCM [REF-PCM]
		//			   3		16-bit linear PCM
		//			   4		24-bit linear PCM
		//			   5		32-bit linear PCM
		//			   6		32-bit IEEE floating point
		//			   7		64-bit IEEE floating point
		//			  23		8-bit ISDN u-law compressed
		//					using the CCITT G.721 ADPCM
		//					voice data encoding scheme.
		int encoding = (in.read() << 24) | (in.read() << 16) | (in.read() << 8) | (in.read());
		System.out.println("enc: " + encoding);

		// sample rate	the number of samples/second (e.g., 8000)
		samplerate = (in.read() << 24) | (in.read() << 16) | (in.read() << 8) | (in.read());
		System.out.println("rate: " + samplerate);
		
		// channels		the number of interleaved channels (e.g., 1)
		int channels = (in.read() << 24) | (in.read() << 16) | (in.read() << 8) | (in.read());
		System.out.println("channels: " + channels);

		in.skip(offset - 24);

		sampledata = new int[size];
		byte tmpsampledata[] = new byte[size];
		int read = in.read(tmpsampledata);

		while (read != size) {
			read += in.read(tmpsampledata, read, size - read);
		}

		// we want linear samples so convert...
		if (encoding == 1) {
			for (int i = 0; i < sampledata.length; i++) {
				sampledata[i] = mulawDecode((int) tmpsampledata[i]);
			}
		}
		in.close();
	}

	private int mulawDecode(int ulaw) {
		ulaw = ~ulaw;
		int exponent = (ulaw >> 4) & 0x7;
		int mantissa = (ulaw & 0xf) + 16;
		int adjusted = (mantissa << (exponent + 3)) - 128 - 4;

		return ((ulaw & 0x80) > 0) ? adjusted : -adjusted;
	}

	/**
	 * Play...
	 */
	public int read(int[] buffer, int off, int len) {
		if (!playLoud) return len;
		for (int i = off; i < len; i++) {
			int currsamp = sampledata[(int) samppos];

			switch (channels) {
				case 1:
					buffer[i] = currsamp & 65535;
					break;
				case 2:
					buffer[i] = (currsamp & 65535) | (currsamp << 16);
					break;
			}
			samppos += pitch;
			if ((int) samppos >= sampledata.length) samppos = 0;
		}
		return len;
	}

	public void setDevice(AudioOutDevice device) {
		super.setDevice(device);
		pitch = ((double) this.samplerate / deviceSampleRate);
	}
}
/*
 * ChangeLog:
 * $Log: Au.java,v $
 * Revision 1.3  2000/12/21 17:16:00  fredde
 * load(is) is public
 *
 * Revision 1.2  2000/09/29 19:39:22  fredde
 * we do not care about volume anymore
 *
 * Revision 1.1.1.1  2000/09/25 16:34:34  fredde
 * initial commit
 *
 */
