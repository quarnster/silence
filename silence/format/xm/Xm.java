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
package org.gjt.fredde.silence.format.xm;

import java.io.*;
import java.net.URL;

import org.gjt.fredde.silence.format.AudioFormat;

/**
 * The general xm class
 *
 * @author Fredrik Ehnbom
 * @version $Id: Xm.java,v 1.3 2000/10/07 13:55:07 fredde Exp $
 */
public class Xm
	extends AudioFormat
{
	private String title = "";
	private String tracker = "";
	int patorder[];

	int defaultTempo = 0;
	int defaultBpm = 0;
	int tempo = 0;

	int samplesPerTick;

	int playingPatternPos = 0;
	int playingPattern = 0;
	int patternPos = 0;

	private int restTick = 0;

	private	Pattern[]	pattern;
	protected Instrument[]	instrument;
	private	Channel[]	channel;

	public Xm() {
	}

	public static final byte[] read(BufferedInputStream in, int len)
		throws IOException
	{
		byte[] b = new byte[len];
		for (int i = 0; i < len; i += in.read(b, i, len-i));
		return b;
	}

	/**
	 * Load the file into memory
 	 *
	 * @param is The InputStream to read the file from
	 */
	protected void load(BufferedInputStream in)
		throws IOException
	{
		readGeneralInfo(in);

		for (int i = 0; i < pattern.length; i++) {
			pattern[i] = new Pattern(in);
		}

		for (int i = 0; i < instrument.length; i++) {
			instrument[i] = new Instrument(in);
		}
		in.close();
	}

	private void readGeneralInfo(BufferedInputStream in)
		throws IOException
	{
		// "Extended Module: "
		byte b[] = read(in, 17);

		if (!(new String(b)).equalsIgnoreCase("Extended Module: ")) {
			throw new IOException("This is not a xm file!");
		}


		// Module name
		b = read(in, 20);

		title = new String(b);
		System.out.println("Title: " + title);

		in.read();

		// Tracker name
		b = read(in, 20);

		System.out.println("Tracker: " + new String(b));
		System.out.println("Version: 1: " + in.read() + ", 2: " + in.read());

		// Header size
		b = read(in, 4);

		// Song length (in pattern order table)
		b = read(in, 2);
		System.out.println("song length: " +  b[0]);
		patorder = new int[b[0]];

		// Restart position
		b = read(in, 2);
		System.out.println("Restart position: " + b[0]);


		// Number of channels (2,4,6,8,10,...,32)
		b = read(in, 2);
		System.out.println("Number of channels: " + b[0]);
		channel = new Channel[b[0]];

		for (int i = 0; i < channel.length; i++) {
			channel[i] = new Channel(this);
		}

		// Number of patterns (max 128)
		b = read(in, 2);
		System.out.println("Number of patterns: " + b[0]);
		pattern = new Pattern[b[0]];

		// Number of instruments (max 128)
		b = read(in, 2);
		instrument = new Instrument[(int) ((b[0] < 0) ? 256 + b[0] : b[0])];
		System.out.println("Number of instruments: " + instrument.length);

		// Flags: bit 0: 0 = Amiga frequency table;
		//               1 = Linear frequency table
		b = read(in, 2);

		// Default tempo
		b = read(in, 2);
		defaultTempo = (int) b[0];
		System.out.println("Default tempo: " + b[0]);

		// Default BPM
		b = read(in, 2);
		int t[] = new int[2];

		t[0] = (int) ((b[0] < 0 ) ? 256 + b[0] : b[0]);
		t[1] = (int) ((b[1] < 0 ) ? 256 + b[1] : b[1]);
		defaultBpm = (t[0] << 0) + (t[1] << 8);
		System.out.println("Default BPM: " + defaultBpm);

		// Pattern order table
		b = read(in, 256);

		System.out.println("Pattern order table: ");
		for (int i = 0; i < patorder.length; i++) {
			if (i+1 != patorder.length) System.out.print(b[i] + ", ");
			else System.out.println(b[i]);

			patorder[i] = b[i];
		}
		playingPatternPos = 0;
		playingPattern = patorder[playingPatternPos];
	}

	/**
	 * Play...
	 */
	public int read(int[] buffer, int off, int len) {
		int realLen = len;
		for (int i = off; i < off+len; i++) {
			buffer[i] = 0;
		}

		if (restTick > 0) {
			int read = restTick > len ? len : restTick;
			for (int j = 0; j < channel.length; j++)  {
				channel[j].play(buffer, off, read);
			}
			off += read;
			len -= read;
			restTick -= read;
		}
			

readLoop:
		for (int i = off; i < off+len; i += samplesPerTick) {
			if (--tempo <= 0) {
				for (int j = 0; j < channel.length; j++)  {
					patternPos = channel[j].update(pattern[playingPattern], patternPos);
					if (patternPos < 0) {
						patternPos = 0;
						tempo = 0;

						continue readLoop;
					}
				}

				if (patternPos == pattern[playingPattern].data.length) {
					patternPos = 0;
					playingPatternPos++;
					if (playingPatternPos == patorder.length) {
						// TODO: end.....
						playingPatternPos = 0;
					}
					playingPattern = patorder[playingPatternPos];
					System.out.println("Pattern: " + playingPattern);
				}
				tempo = defaultTempo;
			}
			int read = samplesPerTick;
			if (read > off+len-i) {
				read = off+len-i;
				restTick = samplesPerTick - read;
			}

			for (int j = 0; j < channel.length; j++)  {
				channel[j].updateTick();
				channel[j].play(buffer, i, read);
			}
		}

		return realLen;
	}

	public void setDevice(org.komplex.audio.AudioOutDevice device) {
		super.setDevice(device);
		samplesPerTick = (5 * deviceSampleRate) / (2 * defaultBpm);
		System.out.println("samples_per_tick: " + samplesPerTick);
		System.out.println("deviceSampleRate: " + deviceSampleRate);
	}
}
/*
 * ChangeLog:
 * $Log: Xm.java,v $
 * Revision 1.3  2000/10/07 13:55:07  fredde
 * Fixed to read in the data correctly.
 * Fixed to play the .xm correctly.
 *
 * Revision 1.2  2000/10/01 17:09:31  fredde
 * added lots of code for acctual playback
 *
 * Revision 1.1.1.1  2000/09/25 16:34:33  fredde
 * initial commit
 *
 */
