/* Instrument.java - Stores information about an instrument
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

import java.awt.Point;
import java.io.*;

/**
 * This class stores information about an instrument
 *
 * @author Fredrik Ehnbom
 * @version $Id: Instrument.java,v 1.6 2003/08/22 06:56:48 fredde Exp $
 */
class Instrument {
	Sample[]	sample;
	Point[]	volumeEnvelopePoints;
	Point2Df[]	volumeEnvInfo;

	int		fadeoutVolume;
	int		volType;
	int		volSustain;
	int		volLoopStart;
	int		volLoopEnd;


	public Instrument(BufferedInputStream in)
		throws IOException
	{
		// Instrument size
		int size = Xm.make32Bit(Xm.read(in, 4));


		// Instrument name
		Xm.read(in, 22);

		// Instrument type (always 0)
		// Note: not always 0 but it says so in the documents...
		in.read();

		// Number of samples in instrument
		sample = new Sample[Xm.make16Bit(Xm.read(in, 2))];

		if (sample.length == 0) {
			Xm.read(in, size - 29);
		} else {
			// Sample header size
			int ssize = Xm.make32Bit(Xm.read(in, 4));
			if (ssize != 40) {
				throw new IOException("samplesize != 40!");
			}


			// Sample number for all notes
			Xm.read(in, 96);

			// Points for volume envelope
			byte[] tmp = Xm.read(in, 48);

			// Points for panning envelope
			Xm.read(in, 48);

			// Number of volume points
			int points = in.read();
			volumeEnvelopePoints = new Point[points]; //new int[points * 2];
			if (points > 0)
				volumeEnvInfo = new Point2Df[points-1];

			int pos = 0;
			for (int i = 0; i < points; i++, pos += 2) {
				volumeEnvelopePoints[i] = new Point();

				volumeEnvelopePoints[i].x = Xm.make16Bit(tmp, pos);
				pos += 2;
				volumeEnvelopePoints[i].y = Xm.make16Bit(tmp, pos);
			}

			for (int i = 0; i < points-1; i ++) {
				volumeEnvInfo[i] = new Point2Df();
				volumeEnvInfo[i].y = (float) (
					volumeEnvelopePoints[i + 0].y -
					volumeEnvelopePoints[i + 1].y
				) /
				(float) (
					volumeEnvelopePoints[i + 0].x -
					volumeEnvelopePoints[i + 1].x
				);

				volumeEnvInfo[i].x = (
						volumeEnvelopePoints[i + 1].x -
		 				volumeEnvelopePoints[i + 0].x
					);
			}

			// Number of panning points 
			in.read();

			// Volume sustain point
			volSustain = in.read();

			// Volume loop start point
			volLoopStart = in.read();

			// Volume loop end point
			volLoopEnd = in.read();

			// Panning sustain point
			in.read();

			// Panning loop start point
			in.read();

			// Panning loop end point
			in.read();

			// Volume type: bit 0: on; 1: Sustain; 2: Loop
			volType = in.read();

			// Panning type: bit 0: on; 1: Sustain; 2: Loop
			in.read();

			// Vibrato type
			in.read();

			// Vibrato sweep
			in.read();

			// Vibrato depth
			in.read();

			// Vibrato rate
			in.read();

			// Volume fadeout
			fadeoutVolume= Xm.make16Bit(Xm.read(in, 2));

			// reserved
			Xm.read(in, 22);

			for (int i = 0; i < sample.length; i++) {
				sample[i] = new Sample(in);
			}
			for (int i = 0; i < sample.length; i++) {
				sample[i].readData(in);
			}
		}
	}
}
/*
 * ChangeLog:
 * $Log: Instrument.java,v $
 * Revision 1.6  2003/08/22 06:56:48  fredde
 * volumeEnvInfo should have one less 'point'
 *
 * Revision 1.5  2000/12/21 17:20:37  fredde
 * precalc k-values for volume envelopes
 *
 * Revision 1.4  2000/10/14 19:10:05  fredde
 * now uses Xm.make[16|32]Bit()
 *
 * Revision 1.3  2000/10/12 15:07:17  fredde
 * removed log messages
 *
 * Revision 1.2  2000/10/07 13:49:08  fredde
 * fixed to read the data correctly
 *
 * Revision 1.1.1.1  2000/09/25 16:34:34  fredde
 * initial commit
 *
 */
