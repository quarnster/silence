/* Au.java - Loading and playing .au-files
 * Copyright (C) 2000-2001 Fredrik Ehnbom
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
 * @version $Id: Au.java,v 1.6 2001/01/11 20:25:37 fredde Exp $
 */
public class Au
	extends AudioFormat
{

	private int samplerate = 0;
	private int channels = 0;

	private double samppos = 0;
	private double pitch = 0;

	private BufferedInputStream in = null;
	private int[] samples = new int[1024];
	private byte[] sampleData = new byte[1024];
	private Decoder decoder;

	public Au() {
	}

	/**
	 * Load the file into memory
	 * @param in The InputStream to read the file from
	 */
	public void load(BufferedInputStream in)
		throws IOException
	{
		this.in = in;
		// magic number	        the value 0x2e736e64 (ASCII ".snd")
		byte[] b = new byte[4];
		in.read(b);

		if (!(new String(b).equals(".snd"))) {
			throw new IOException("this does not seem to be an .au file...");
		}

		// data offset          the offset, in octets, to the data part.
		//			The minimum valid number is 24 (decimal).
		int offset = (in.read() << 24) | (in.read() << 16) | (in.read() << 8) | (in.read());

		// data size		the size in octets, of the data part.
		//			If unknown, the value 0xffffffff should
		//			be used.
		int size = (in.read() << 24) | (in.read() << 16) | (in.read() << 8) | (in.read());

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

		switch (encoding) {
			case 1:
				decoder = new MulawDecoder();
				break;
			default:
				throw new IOException("file has unknown encoding...");
		}

		// sample rate          the number of samples/second (e.g., 8000)
		samplerate = (in.read() << 24) | (in.read() << 16) | (in.read() << 8) | (in.read());

		// channels		the number of interleaved channels (e.g., 1)
		channels = (in.read() << 24) | (in.read() << 16) | (in.read() << 8) | (in.read());

		in.skip(offset - 24);
	}

	/**
	 * Play...
	 */
	public int read(int[] buffer, int off, int len) {
		try {
			for (int i = off; i < off+len; ) {
	        		if (samppos >= samples.length) {
		        		int ret = in.read(sampleData);
					samples = decoder.decode(sampleData);
					samppos = 0;

					// end of stream
					if (ret == 0) return -1;
			        }

				int pos = 0;
				if (channels == 2) {
					for (; i < (off + len) && samppos < samples.length; samppos += pitch) {
						pos =  (((int) samppos) >> 1) << 1;

		        			buffer[i] = samples[pos] & 65535;
			        		buffer[i++] |= samples[pos+1] << 16;
					}

				} else {
					for (; i < (off + len) && samppos < sampleData.length; samppos += pitch) {
						pos =  (int) samppos;
						int sample = samples[pos];

		        			buffer[i] = sample & 65535;
			        		buffer[i++] |= sample << 16;
					}
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return -1;
		}

		return len;
	}

	public void setDevice(AudioOutDevice device) {
		super.setDevice(device);
		pitch = device.getChannels() * ((double) this.samplerate / device.getSampleRate());
	}

	public void close() {
		try {
			in.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public String toString() {
		return "AU";
	}
}
/*
 * ChangeLog:
 * $Log: Au.java,v $
 * Revision 1.6  2001/01/11 20:25:37  fredde
 * added custom toString
 *
 * Revision 1.5  2001/01/08 19:49:35  fredde
 * updated now that the AudioFormat just saves the device
 * instead of lots of data in different variables
 *
 * Revision 1.4  2001/01/06 10:41:15  fredde
 * streams data, stereo working, using decoders
 *
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
