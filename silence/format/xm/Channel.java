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
package org.gjt.fredde.silence.format.xm;

/**
 * A class that stores channel data
 *
 * @version $Id: Channel.java,v 1.3 2000/10/01 17:06:38 fredde Exp $
 * @author Fredrik Ehnbom
 */
class Channel {
	private Xm xm;

	public Channel(Xm xm) {
		this.xm = xm;
	}

	private int currentNote		= 0;
	private int currentInstrument	= 0;
	private int currentVolume	= 0;
	private int currentEffect	= 0;
	private int currentEffectParam = 0;
	private double currentPitch 	= 0;
	private double currentPos	= 0;

	private final double calcPitch(int note) {
		note += xm.instrument[currentInstrument].sample[0].relativenote;

		double period = 10*12*16*4 - note*16*4 - xm.instrument[currentInstrument].sample[0].finetune/2;
		double freq = 8363 * Math.pow(2, ((6 * 12 * 16 * 4 - period) / (12 * 16 * 4)));
		double per = 1 / ((double) xm.deviceSampleRate / freq);

		return per;
	}

	final int update(Pattern pattern, int patternpos) {
		int check = pattern.data[patternpos++];
		if ((check & 0x80) != 0) {
			// note
			if ((check & 0x1) != 0) {
				currentNote = pattern.data[patternpos++];
				currentPitch = calcPitch(currentNote);
				currentPos = 0;
			}

			// instrument
			if ((check & 0x2) != 0) currentInstrument = pattern.data[patternpos++] - 1;

			// volume
			if ((check & 0x4) != 0) currentVolume = pattern.data[patternpos++];

			// effect
			if ((check & 0x8) != 0) currentEffect = pattern.data[patternpos++];
			else currentEffect = 0;

			// effect param
			if ((check & 0x10) != 0) currentEffectParam = pattern.data[patternpos++];
			else currentEffectParam = 0;
		} else {
			currentNote			= check;
			currentInstrument	= pattern.data[patternpos++] - 1;
			currentVolume		= pattern.data[patternpos++];
			currentEffect		= pattern.data[patternpos++];
			currentEffectParam	= pattern.data[patternpos++];
			currentPitch = calcPitch(currentNote);
			currentPos = 0;

		}
		switch (currentEffect) {
			case 0x0F:	// set tempo
				if (currentEffectParam > 0x20) {
					xm.default_bpm = currentEffectParam;
					xm.samples_per_tick = (5 * xm.deviceSampleRate) / (2 * xm.default_bpm);
				} else {
					xm.default_tempo = currentEffectParam;
					xm.tempo = xm.default_tempo;
				}
				break;
		}
		return patternpos;
	}

	final void play(int[] buffer, int off, int len) {
		if (currentNote == 97 || currentNote == 0) return;
		for (int i = off; i < off+len; i++) {
			buffer[i] += (xm.instrument[currentInstrument].sample[0].sampleData[(int) currentPos] * 128 & 65535) | (xm.instrument[currentInstrument].sample[0].sampleData[(int) currentPos] * 128 << 16);

			currentPos += currentPitch;
			if ( ((int) currentPos) >= xm.instrument[currentInstrument].sample[0].sampleData.length) {
				currentPos = 0;
			}
		}
	}

}
/*
 * ChangeLog:
 * $Log: Channel.java,v $
 * Revision 1.3  2000/10/01 17:06:38  fredde
 * basic playing abilities added
 *
 * Revision 1.2  2000/09/29 19:39:48  fredde
 * no need to be public
 *
 * Revision 1.1.1.1  2000/09/25 16:34:34  fredde
 * initial commit
 *
 */
