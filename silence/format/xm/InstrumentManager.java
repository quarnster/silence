/* $Id: InstrumentManager.java,v 1.5 2003/08/23 13:44:59 fredde Exp $
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
 * @version $Revision: 1.5 $
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


	private int	currentNote = 0;
	private int	currentPitch = 0;
	private int	currentPos = 0;

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
		active = false;
	}

	final int getPeriod(int note) {
		if (currentInstrument == null || currentInstrument.sample.length == 0) return 0;
		note += (currentInstrument.sample[0].relativeNote - 1);

		return  (10*12*16*4) - (note*16*4) - (currentInstrument.sample[0].fineTune / 2) + porta;
	}
	final double calcPitch(int note) {
		int period = getPeriod(note);

		double freq = 8363d * Math.pow(2d, ((6d * 12d * 16d * 4d - period) / (double) (12 * 16 * 4)));
		double pitch = (freq / (double) xm.deviceSampleRate);

		return  pitch;
	}

	public void release() {
		release = true;
		volumeEnvelope.release();
	}

	public void setNote(int note) {
		currentNote = note;
		currentPitch = (int) (calcPitch(note) * 1024);
	}

	public void playNote(int note) {
		if (currentInstrument == null || currentInstrument.sample.length == 0) return;
		porta = 0;
		setNote(note);
		trigger();
	}

	public void setPosition(int position) {
		if (currentInstrument != null && position < currentInstrument.sample[0].sampleData.length) {
			currentPos = position << 10;
		}
	}

	public void trigger() {
		porta = 0;
		release = false;
		currentPos = 0;
		active = true;
		currentVolume = 64;
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
				currentPos = (s.loopStart + s.loopEnd) + currentPos;
				if (currentPos >> 10 == s.sampleData.length) currentPos = s.loopStart + s.loopEnd -1;
			}
			currentPitch = - currentPitch;
		}
	}

	public void play(int[] buffer, int off, int len) {
		if (!active || currentInstrument == null/* || currentNote == 0 || finalVol < 0.01*/) return;
		Sample s = currentInstrument.sample[0];
		if (s.sampleData.length == 0) return;


		for (int i = off; i < off+len; i++) {

			if (currentPos >> 10 >= s.sampleData.length || currentPos < 0) {
				System.out.println("BUG!! xm.playingPatternPos: " + xm.playingPatternPos + ", currentPos: " + (currentPos>>10) + ", length: " + currentInstrument.sample[0].sampleData.length + ", loop: " + (s.loopEnd>>10) + ", currentPitch:  " + currentPitch + ", currentLoopStart: " + (s.loopStart>>10) + ", lt: " + (s.loopType & 0x3));
			}
			int sample = (int) (s.sampleData[(int) (currentPos>>10)] * finalVol)&0xffff;
			int right = ((buffer[i] >> 16)&0xffff);
			int left  = (buffer[i] & 0xffff);

			buffer[i] += sample << 16 | sample; // (clamp(sample+left)) << 16 | (clamp(sample+right));

			currentPos += currentPitch;


			if (currentPitch < 0 && currentPos < s.loopStart) {
				pingpong(s);
			} else if (currentPos >= (s.loopStart + s.loopEnd) || currentPos>>10 >= s.sampleData.length) {
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

	public final void updateVolumes() {
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
	}

}
/*
 * ChangeLog:
 * $Log: InstrumentManager.java,v $
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
