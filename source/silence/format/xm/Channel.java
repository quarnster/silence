/* Channel.java - Stores channel data
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

/**
 * A class that stores channel data
 *
 * @version $Id: Channel.java,v 1.1 2000/09/03 17:42:56 quarn Exp $
 * @author Fredrik Ehnbom
 */
public class Channel {

	private Xm xm;

	public Channel(Xm xm) {
		this.xm = xm;
	}

	private int currentInstrument	= 0;
	private int currentNote		= 0;
	private int currentEffect	= 0;
	private double currentPitch 	= 0;
	private double currentPos	= 0;

	private final double calcPitch(int note) {
		double period = 10*12*16*4 - note*16*4 - xm.instrument[currentInstrument].sample[0].finetune/2;
		double freq = 8363 * Math.pow(2, ((6 * 12 * 16 * 4 - period) / (12 * 16 * 4)));
		double per = 1 / ((double) xm.deviceSampleRate / freq);

		return per;
	}

	protected final int update(Pattern pattern, int patternPos, int[] buffer, int off, int len) {
		// just a little test...
		// plays the first instrument in different notes

		if (currentPitch == 0) {
			currentNote = 60;
			currentPitch = calcPitch(currentNote);
		}

		for (int i = off; i < off+len; i++) {
			buffer[i] = (xm.instrument[currentInstrument].sample[0].sampleData[(int) currentPos] * 128 & 65535) | (xm.instrument[currentInstrument].sample[0].sampleData[(int) currentPos] * 128 << 16);

			currentPos += currentPitch;
			if ( ((int) currentPos) >= xm.instrument[currentInstrument].sample[0].sampleData.length) {
				currentPos = 0;

				currentNote++;
				currentNote = (currentNote > 95) ? 60 : currentNote;
				currentPitch = calcPitch(currentNote);
			}
		}
		return 0;
	}

}
/*
 * ChangeLog:
 * $Log: Channel.java,v $
 * Revision 1.1  2000/09/03 17:42:56  quarn
 * Channel handler
 *
 */
