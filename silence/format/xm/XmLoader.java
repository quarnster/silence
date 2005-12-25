/* XmLoader.java - handles loading of XM-files
 * Copyright (C) 2000-2005 Fredrik Ehnbom
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
import java.awt.Point;

import silence.format.xm.data.*;

/**
 * Loads Xm files
 *
 * @author Fredrik Ehnbom
 */
public class XmLoader
{
	public XmLoader() {
	}

	public static final byte[] read(BufferedInputStream in, int len)
		throws IOException
	{
		byte[] b = new byte[len];
		for (int i = 0; i < len; i += in.read(b, i, len-i));
		return b;
	}

	public static final int make32Bit(byte[] b) {
		return make32Bit(b, 0);
	}
	public static final int make32Bit(byte[] b, int off) {
		return (
				((b[off + 0] & 0xff) << 0) +
				((b[off + 1] & 0xff) << 8) +
				((b[off + 2] & 0xff) << 16) +
				((b[off + 3] & 0xff) << 24)
			);
	}

	public static final int make16Bit(byte[] b) {
		return make16Bit(b, 0);
	}

	public static final int make16Bit(byte[] b, int off) {
		return (
				((b[off + 0] & 0xff) << 0) +
				((b[off + 1] & 0xff) << 8)
			);
	}

	private void readPattern(BufferedInputStream in, Pattern p)
		throws IOException
	{
		// Pattern header length
		read(in, 4);

		// Packing type (always 0)
		in.read();

		// Number of rows in pattern (1...256)
		int rows = make16Bit(read(in, 2));

		// Packed patterndata size
		int size = make16Bit(read(in, 2));

		int data[];
		if (size == 0) {
			data = new int[0];
		} else {
			byte[] b = read(in, size);
			data = new int[size];
			for (int i = 0; i < size; i++) {
				data[i] = b[i];
			}
		}
		p.setData(data);
		p.setRows(rows);
	}

	private Point[] makeEnvelopePoints(int count, byte[] data) {
		Point[] envelopePoints = new Point[count];

		int pos = 0;
		for (int i = 0; i < count; i++, pos += 2) {
			envelopePoints[i] = new Point();

			envelopePoints[i].x = make16Bit(data, pos);
			pos += 2;
			envelopePoints[i].y = make16Bit(data, pos);
		}
		return envelopePoints;
	}
	public void readInstrument(BufferedInputStream in, Instrument instr) 
		throws IOException
	{
		// Instrument size
		int size = make32Bit(read(in, 4));


		// Instrument name
		read(in, 22);

		// Instrument type (always 0)
		// Note: not always 0 but it says so in the documents...
		in.read();

		// Number of samples in instrument
		Sample[] sample = new Sample[make16Bit(read(in, 2))];

		if (sample.length == 0) {
			read(in, size - 29);
		} else {
			// Sample header size
			int ssize = make32Bit(read(in, 4));
			if (ssize != 40) {
				throw new IOException("samplesize != 40!");
			}


			// Sample number for all notes
			instr.setSampleForNote(read(in, 96));

			// Points for volume envelope
			byte[] volEnvData = read(in, 48);

			// Points for panning envelope
			byte[] panEnvData = read(in, 48);

			// Number of volume points
			int points = in.read();
			Envelope volumeEnvelope = new Envelope();
			volumeEnvelope.setData(makeEnvelopePoints(points, volEnvData));

			// Number of panning points 
			points = in.read();
			Envelope panningEnvelope = new Envelope();
			panningEnvelope.setData(makeEnvelopePoints(points, panEnvData));

			// Volume sustain point
			volumeEnvelope.setSustainPosition(in.read());

			// Volume loop start point
			volumeEnvelope.setLoopStart(in.read());

			// Volume loop end point
			volumeEnvelope.setLoopEnd(in.read());

			// Panning sustain point
			panningEnvelope.setSustainPosition(in.read());

			// Panning loop start point
			panningEnvelope.setLoopStart(in.read());

			// Panning loop end point
			panningEnvelope.setLoopEnd(in.read());

			// Volume type: bit 0: on; 1: Sustain; 2: Loop
			volumeEnvelope.setType(in.read());

			// Panning type: bit 0: on; 1: Sustain; 2: Loop
			panningEnvelope.setType(in.read());

			// Vibrato type
			Vibrato vibrato = new Vibrato();
			vibrato.setType(in.read());

			// Vibrato sweep
			vibrato.setSweep(in.read());

			// Vibrato depth
			vibrato.setDepth(in.read());

			// Vibrato rate
			vibrato.setRate(in.read());

			// Volume fadeout
			instr.setFadeout(make16Bit(read(in, 2)));

			// reserved
			read(in, 22);

			for (int i = 0; i < sample.length; i++) {
				sample[i] = new Sample();
				loadSample(in, sample[i]);
			}
			for (int i = 0; i < sample.length; i++) {
				loadSampleData(in, sample[i]);
			}

			instr.setPanningEnvelope(panningEnvelope);
			instr.setVolumeEnvelope(volumeEnvelope);
			instr.setVibrato(vibrato);
			instr.setSamples(sample);
		}
	}

	private void loadSample(BufferedInputStream in, Sample sample)
		throws IOException
	{
		// Sample length
		int sampleLength = make32Bit(read(in, 4));

		// Sample loop start
		int loopStart = make32Bit(read(in, 4));

		// Sample loop length
		int loopEnd = make32Bit(read(in, 4));

		if (loopStart + loopEnd > sampleLength)
			loopEnd = (sampleLength) - loopStart;

		// Volume
		sample.setVolume(in.read());

		// Finetune (signend byte -128...+127)
		sample.setFineTune((byte) in.read());

		// Type: Bit 0-1: 0 = No loop,
		//                1 = Forward loop,
		//		  2 = Ping-pong loop;
		//                4: 16-bit sampledata
		int loopType = in.read();
		int sampleQuality = ((int) loopType & 0x10) != 0 ? 16 : 8;

		if ((loopType & 0x3) == 0 || loopEnd == 0) {
			// no looping
			loopStart = 0;
			loopEnd = sampleLength;
			loopType &= 0x10;
		}

		// Panning (0-255)
		sample.setPanning(in.read()&0xff);

		// Relative note number (signed byte)
		sample.setRelativeNote((byte) in.read());

		// Reserved
		in.read();

		// Sample name
		read(in, 22);

		sample.setLength(sampleLength);
		sample.setLoopType(loopType);
		sample.setQuality(sampleQuality);
		sample.setLoopStart(loopStart);
		sample.setLoopEnd(loopEnd);

	}

	private void loadSampleData(BufferedInputStream in, Sample sample)
		throws IOException
	{
		int sampleLength = sample.getLength();
		int loopEnd = sample.getLoopEnd();
		int loopStart = sample.getLoopStart();
		short[] sampleData;
		if (sample.getQuality() == 16) {
			sampleLength >>= 1;
			loopEnd >>= 1;
			loopStart >>= 1;

			byte[] temp = read(in, 2 * sampleLength);
			sampleData = new short[sampleLength+4];

			int tmpPos = 0;

			int samp = 0;

			for (int i = 0; i < sampleLength; i++, tmpPos += 2) {
				samp += make16Bit(temp, tmpPos);
				sampleData[i] = (short) (samp);
			}
		} else {
			sampleData = new short[sampleLength+4];
			byte[] temp = read(in, sampleLength);
			int samp = 0;

			for (int i = 0; i < sampleLength; i++) {
				samp += temp[i]&0xff;
				sampleData[i] = (short) (samp << 8);
			}
		}


		if (sampleLength > 0) {
			int pos2 = 0;
			int loopType = sample.getLoopType();
			if ((loopType & Sample.PINGPONG_LOOP) == 0) {
				if ((loopType & Sample.FORWARD_LOOP) != 0)
					pos2 = loopStart;

				for (int i = 0; i < 3; i++) 
					sampleData[sampleLength+1+i] = sampleData[pos2+i];

			} else if ((loopType & Sample.PINGPONG_LOOP) != 0) {
				pos2 = loopStart + loopEnd;

				for (int i = 0; i < 3; i++)
					sampleData[sampleLength+1+i] = sampleData[pos2-1-i];

			}
			System.arraycopy(sampleData, 0, sampleData, 1, sampleLength);

			if ((loopType & Sample.FORWARD_LOOP) != 0) sampleData[0] = sampleData[loopStart+loopEnd];
			else if ((loopType & Sample.PINGPONG_LOOP) != 0) sampleData[0] = sampleData[loopStart+2];
		}

		loopStart <<= 10;
		loopEnd <<= 10;

		sample.setLoopStart(loopStart);
		sample.setLoopEnd(loopEnd);
		sample.setLength(sampleLength);
		sample.setData(sampleData);
	}

	/**
	 * Load the file into memory
 	 *
	 * @param is The InputStream to read the file from
	 */
	public void load(BufferedInputStream in, Module module)
		throws IOException
	{
		readGeneralInfo(in, module);

		Pattern[] pattern = module.getPatterns();
		for (int i = 0; i < pattern.length; i++) {
			pattern[i] = new Pattern();
			readPattern(in, pattern[i]);
		}

		Instrument[] instrument = module.getInstruments();
		for (int i = 0; i < instrument.length; i++) {
			instrument[i] = new Instrument();
			readInstrument(in, instrument[i]);
		}
		in.close();
	}

	private void readGeneralInfo(BufferedInputStream in, Module module)
		throws IOException
	{
		// "Extended Module: "
		byte b[] = read(in, 17);

		if (!(new String(b)).equalsIgnoreCase("Extended Module: ")) {
			throw new IOException("This is not a xm file!");
		}

		// Module name
		b = read(in, 20);
		module.setTitle(new String(b));

		in.read();

		// Tracker name
		b = read(in, 20);

		// version
		in.read();
		in.read();

		// Header size
		read(in, 4);

		// Song length (in pattern order table)
		int[] patorder = new int[make16Bit(read(in, 2))];

		// Restart position
		module.setRestartPosition(make16Bit(read(in, 2)));

		// Number of channels (2,4,6,8,10,...,32)
		module.setChannelCount(make16Bit(read(in, 2)));

		// Number of patterns (max 128)
		module.setPatterns(new Pattern[make16Bit(read(in, 2))]);

		// Number of instruments (max 128)
		module.setInstruments(new Instrument[make16Bit(read(in, 2))]);

		// Flags: bit 0: 0 = Amiga frequency table;
		//               1 = Linear frequency table
		if ((make16Bit(read(in, 2)) & 0x1) == 0)
			module.setAmigaFreqTable(true);

		// Default tempo
		module.setTempo(make16Bit(read(in, 2)));

		// Default BPM
		module.setBpm(make16Bit(read(in, 2)));

		// Pattern order table
		b = read(in, 256);

		for (int i = 0; i < patorder.length; i++) {
			patorder[i] = b[i];
		}
		module.setPatternOrder(patorder);
	}
}
