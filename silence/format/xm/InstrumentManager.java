/* $Id: InstrumentManager.java,v 1.6 2003/09/01 09:05:14 fredde Exp $
 * Copyright (C) 2000-2003 Fredrik Ehnbom
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
 * This class handles the playing of an instrument
 *
 * @author Fredrik Ehnbom
 * @version $Revision: 1.6 $
 */
public class InstrumentManager {
	Xm		xm;
	int		fadeOutVol;

	float		volEnv			= 64;
	Envelope volumeEnvelope			= new Envelope();

	private Instrument currentInstrument = null;

	int currentVolume;
	float rowVol = 0;
	float finalVol = 0;


	private int	notePitch = 0;
	private int	currentNote = 0;
	private int	currentPitch = 0;
	private int	currentPos = 0;

	double	freq		= 0;
	int	freqDelta	= 0;
	int	period		= 0;

	private final float volumeScale = 0.25f;

	boolean active = false;
	boolean release = false;
	int porta = 0;

	public InstrumentManager(Xm xm) {
		this.xm = xm;
	}

	public void setInstrument(Instrument instrument) {
		if (instrument == null || instrument.sample.length == 0) {
			currentInstrument = null;
		} else {
			currentInstrument = instrument;
			volumeEnvelope.setData(instrument);
		}
		currentVolume = 64;
		active = false;
	}

	final int getPeriod(int note) {
		if (currentInstrument == null || currentInstrument.sample.length == 0) return 0;
		note += (currentInstrument.sample[0].relativeNote - 1);

		return  (10*12*16*4) - (note*16*4) - (currentInstrument.sample[0].fineTune / 2) + porta;
	}
	final double calcPitch(int note) {
		period = getPeriod(note);

		freq = 8363d * Math.pow(2d, ((6d * 12d * 16d * 4d - period) / (double) (12 * 16 * 4)));
		double pitch = (freq / (double) xm.deviceSampleRate);

		return  pitch;
	}

	public void release() {
		release = true;
		volumeEnvelope.release();
	}

	public void setNote(int note) {
		currentNote = note;
		notePitch = (int) (calcPitch(note) * 1024);
	}

	public void playNote(int note) {
		if (currentInstrument == null || currentInstrument.sample.length == 0) return;
		porta = 0;
//		currentVolume = 64;
		currentPitch = 0;
		setNote(note);
		trigger();
	}

	public void setPosition(int position) {
		if (currentInstrument != null && position < currentInstrument.sample[0].sampleData.length) {
			currentPos = position << 10;
		}
	}

	public void trigger() {
		vibPos = 0;
		vibSweepPos = 0;
		porta = 0;
		release = false;
		currentPos = 0;
		active = true;
//		currentVolume = 64;
		fadeOutVol = 65536;
		volumeEnvelope.reset();
	}

	private void pingpong(Sample s) {
		while ((currentPitch < 0 && currentPos < s.loopStart) || ((currentPos >= s.loopStart + s.loopEnd) && currentPitch > 0)) {
			if (currentPitch < 0) {
				currentPos = s.loopStart - currentPos;
				currentPos += s.loopStart;
			} else {
				currentPos = (s.loopStart + s.loopEnd) - currentPos;
				currentPos = (s.loopStart + s.loopEnd-1) + currentPos;
//				if (currentPos >> 10 == s.sampleLength) currentPos = s.loopStart + s.loopEnd -1;
			}
			currentPitch = - currentPitch;
		}
	}

	private final static int clamp(int src) {
		src = src < -32768 ? -32768 : src > 32767 ? 32767 : src;
		return src;
	}

	public void play(int[] buffer, int off, int len) {
		if (!active || currentInstrument == null/* || currentNote == 0 || finalVol < 0.01*/) return;
		Sample s = currentInstrument.sample[0];
		if (s.sampleData.length == 0) return;


		for (int i = off; i < off+len; i++) {
			int pos = currentPos >> 10;

			if (pos >= s.sampleLength) {
				System.out.println("BUG!! xm.playingPatternPos: " + xm.playingPatternPos + ", currentPos: " + (currentPos>>10) + ", length: " + s.sampleLength + ", loop: " + (s.loopEnd>>10) + ", currentPitch:  " + currentPitch + ", currentLoopStart: " + (s.loopStart>>10) + ", lt: " + (s.loopType & 0x3));
			}
			pos++;
			int sample = 0;

			float finpos = Math.abs(currentPitch / 1024.0f);
			if (finpos >= 1) {
				sample = (int) (s.sampleData[pos] * finalVol);
			} else {
				short xm1 = s.sampleData[pos - 1];
				short x0  = s.sampleData[pos + 0];
				short x1  = s.sampleData[pos + 1];
				short x2  = s.sampleData[pos + 2];
				float a = (3 * (x0-x1) - xm1 + x2) / 2.0f;
				float b = 2*x1 + xm1 - (5*x0 + x2) / 2.0f;
				float c = (x1 - xm1) / 2.0f;
				sample = (int) (((((a * finpos) + b) * finpos + c) * finpos + x0) * finalVol);
			}

//			sample = (int) (s.sampleData[pos] * finalVol);


			buffer[i] += sample; //clamp(sample+left) << 16 | clamp(sample+right); // (clamp(sample+left)) << 16 | (clamp(sample+right));

			currentPos += currentPitch;


			if (currentPitch < 0 && currentPos < s.loopStart) {
				pingpong(s);
			} else if (currentPos >= (s.loopStart + s.loopEnd) || currentPos >> 10 >= s.sampleLength) {
				if ((s.loopType & 0x2) != 0) {
					// pingpong loop
					pingpong(s);
				} else if ((s.loopType & 0x1) != 0) {
					// forward loop
					currentPos -= s.loopStart + s.loopEnd;
					currentPos %= s.loopEnd;

					currentPos += s.loopStart;
				} else {
					// no loop
					active = false;
					return;
				}
			}
		}
	}

	public final void update() {
		if (currentInstrument == null) return;
		rowVol = (( currentVolume / 64f) * volumeScale);
		if (currentInstrument != null && currentInstrument.sample.length > 0) rowVol *= (currentInstrument.sample[0].volume / 64f);
		finalVol = rowVol;

		volEnv = volumeEnvelope.getValue();
		finalVol *= (volEnv / 64);
		if (xm.globalVolume != 64) finalVol *= ((double) xm.globalVolume / 64);


		if (release) {
			if (!volumeEnvelope.use()) {
				active = false;
			} else {
				finalVol *= ((float) fadeOutVol / 65536);
				fadeOutVol -= currentInstrument.fadeoutVolume;
				if (fadeOutVol <= 10) {
					active = false;
					return;
				}
			}
		}
//		currentPitch = notePitch;
		period = getPeriod(currentNote);
		doVibrato();

		notePitch = (int) (((freq+freqDelta) / (double) xm.deviceSampleRate) * 1024);

		if (currentPitch < 0)
			currentPitch = -notePitch;
		else
			currentPitch = notePitch;
	}

	int vibPos = 0;
	int vibSweepPos = 0;
	void doVibrato() {
		int delta = 0;

		switch (currentInstrument.vibType & 3) {
			case 0: delta = (int) (Math.sin(2* Math.PI * vibPos / 256.0f) * 64);
				break;
			case 1:
				delta = 64;
				if (vibPos > 127)
					delta = -64;
			case 2: delta = (128 - ((vibPos + 128) % 256)) >> 1;
				break;
			case 3: delta = (128 - ((256 - vibPos)+128)%256) >> 1;
				break;
		};

		delta *= currentInstrument.vibDepth;
		if (currentInstrument.vibSweep != 0)
			delta = delta * vibSweepPos / currentInstrument.vibSweep;
		delta >>=6;

//		int period = getPeriod(currentNote);

		freq = 8363d * Math.pow(2d, ((6d * 12d * 16d * 4d - period) / (double) (12 * 16 * 4))) + delta;
//		freq += delta;

		vibSweepPos++;
		if (vibSweepPos > currentInstrument.vibSweep)
			vibSweepPos = currentInstrument.vibSweep;

		vibPos += currentInstrument.vibRate;

		if (vibPos > 255)
			vibPos -= 256;
	}

}
/*
 * ChangeLog:
 * $Log: InstrumentManager.java,v $
 * Revision 1.6  2003/09/01 09:05:14  fredde
 * vibrato, cubic spline etc
 *
 * Revision 1.5  2003/08/23 13:44:59  fredde
 * moved envelope stuff from InstrumentManager to Envelope
 *
 * Revision 1.4  2003/08/23 07:35:25  fredde
 * porta implemented, volumeenvelope fixes, release fixes
 *
 * Revision 1.3  2003/08/22 12:39:07  fredde
 * volume envelopfix
 *
 * Revision 1.2  2003/08/22 06:54:49  fredde
 * loop fixes
 *
 * Revision 1.1  2003/08/21 09:25:35  fredde
 * moved instrument-playing from Channel into InstrumentManager
 *
 */
