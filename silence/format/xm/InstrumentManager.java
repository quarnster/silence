/* $Id: InstrumentManager.java,v 1.1 2003/08/21 09:25:35 fredde Exp $
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
 * @version $Revision: 1.1 $
 */
public class InstrumentManager {
	Xm		xm;
	int		fadeOutVol;

	// volume envelope playing info
	boolean		useVolEnv		= false;
	boolean		sustain			= false;
	float		volEnvK			= 0;
	float		volEnv			= 64;
	int		volEnvLoopLen		= 0;
	int		volEnvLength		= 0;
	int		volEnvType		= 0;
	int		volEnvPos		= 0;
	int		volEnvSustain		= 0;


	private Instrument currentInstrument = null;

	int currentVolume;
	float rowVol = 0;
	float finalVol = 0;


	private int	currentNote = 0;
	private int	currentPitch = 0;
	private int	currentPos = 0;

	private final float volumeScale = 0.25f;

	private boolean active = false;

	public InstrumentManager(Xm xm) {
		this.xm = xm;
	}

	public void setInstrument(Instrument instrument) {
		if (instrument.sample.length != 0) currentInstrument = instrument;
		else {
			currentInstrument = null;
			active = false;
		}
	}

	private final double calcPitch(int note) {
		if (currentInstrument.sample.length == 0) return 0;
		note += (currentInstrument.sample[0].relativeNote - 1);

		int period = (10*12*16*4) - (note*16*4) - (currentInstrument.sample[0].fineTune / 2);

		double freq = 8363d * Math.pow(2d, ((6d * 12d * 16d * 4d - period) / (double) (12 * 16 * 4)));
		double pitch = (freq / (double) xm.deviceSampleRate);

		return  pitch;
	}

	public void release() {
		currentNote = 97;
	}

	public void playNote(int note) {
		if (currentInstrument == null || currentInstrument.sample.length == 0) return;
		currentNote = note;
		currentPitch = (int) (calcPitch(note) * 1024);
		trigger();
	}

	public void trigger() {
		currentPos = 0;
		active = true;
		currentVolume = 64;
		fadeOutVol = 65536;

		if (currentInstrument.volumeEnvelopePoints.length != 0) {
			volEnv		= currentInstrument.volumeEnvelopePoints[0].y;
			volEnvPos	= 0;
			volEnvK		= currentInstrument.volumeEnvInfo[volEnvPos].y;
			volEnvLength	= (int) currentInstrument.volumeEnvInfo[volEnvPos].x;
			volEnvSustain	= currentInstrument.volSustain;
			volEnvType	= currentInstrument.volType;
			useVolEnv	= ((volEnvType & 0x1) != 0);

			if ((volEnvType & 0x4) != 0)
				volEnvLoopLen = currentInstrument.volLoopEnd;
			else
				volEnvLoopLen = currentInstrument.volumeEnvelopePoints.length - 1;

			sustain 	= (volEnvPos == volEnvSustain && (volEnvType & 0x2) != 0);
		} else {
			useVolEnv	= false;
			volEnv		= 64;
			volEnvK		= 0;
			volEnvPos	= 0;
		}

	}

	public void play(int[] buffer, int off, int len) {
		if (!active || currentNote == 0 || finalVol < 0.01) return;
		Sample s = currentInstrument.sample[0];

		for (int i = off; i < off+len; i++) {
			if (currentPos >> 10 >= s.sampleData.length || currentPos < 0) {
				System.out.println("BUG!! currentPos: " + (currentPos>>10) + ", length: " + currentInstrument.sample[0].sampleData.length + ", loop: " + (s.loopEnd>>10) + ", currentPitch:  " + currentPitch + ", currentLoopStart: " + (s.loopStart>>10) + ", lt: " + (s.loopType & 0x3));
			}
			int sample = (int) (s.sampleData[(int) (currentPos>>10)] * finalVol)&0xffff;
			short right = (short) ((buffer[i] >> 16)&0xffff);
			short left  = (short) (buffer[i] & 0xffff);

			buffer[i] += sample << 16 | sample;


			currentPos += currentPitch;

			if (currentPos < s.loopStart) {
				if (currentPitch < 0) {
					currentPitch = - currentPitch;
					currentPos = s.loopStart - currentPos;
					currentPos += s.loopStart;
				}
			} else if (currentPos >= (s.loopStart + s.loopEnd) || currentPos>>10 >= s.sampleData.length) {
				if ((s.loopType & 0x2) != 0) {
					// pingpong loop
					currentPitch = -currentPitch;

					currentPos = (s.loopStart + s.loopEnd) - currentPos;
					currentPos = (s.loopStart + s.loopEnd) + currentPos;
					if (currentPos >> 10 == s.sampleData.length) currentPos = s.loopEnd -1;

				} else if ((s.loopType & 0x1) != 0) {
					// forward loop
					currentPos += /*s.loopStart*/ - s.loopEnd;
//					System.out.println("currentPos: " + (currentPos>>10) + ", length: " + currentInstrument.sample[0].sampleData.length + ", loop: " + (s.loopEnd>>10) + ", currentPitch:  " + currentPitch + ", currentLoopStart: " + (s.loopStart>>10) + ", lt: " + (s.loopType & 0x3));
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

		if (currentNote == 97) {
			finalVol *= ((float) fadeOutVol / 65536);
			fadeOutVol -= currentInstrument.fadeoutVolume;
			if (fadeOutVol <= 10) {
				active = false;
				return;
			}
		}
		finalVol *= (volEnv / 64);
		if (xm.globalVolume != 64) finalVol *= ((double) xm.globalVolume / 64);

		if (useVolEnv) {
			if (currentNote == 97) {
				volEnv += volEnvK;
				if (volEnvLength <= 0) {
					volEnvPos++;

					if (volEnvPos == volEnvLoopLen) {
						if ((volEnvType & 0x4) != 0) {
							volEnvPos = currentInstrument.volLoopStart;
							volEnv = currentInstrument.volumeEnvelopePoints[volEnvPos].y;
							volEnvK = currentInstrument.volumeEnvInfo[volEnvPos].y;
							volEnvLength = (int) currentInstrument.volumeEnvInfo[volEnvPos].x;
						} else {
							volEnv = currentInstrument.volumeEnvelopePoints[volEnvPos].y;
							volEnvK = 0;
							useVolEnv = false;
							if (volEnv <= 1) {
								active = false;
								return;
							}
						}
					} else {
						volEnv = currentInstrument.volumeEnvelopePoints[volEnvPos].y;
						volEnvK = currentInstrument.volumeEnvInfo[volEnvPos].y;
						volEnvLength = (int) currentInstrument.volumeEnvInfo[volEnvPos].x;
					}
				}
				volEnvLength--;
			} else if (!sustain) { // note != 97
				volEnv += volEnvK;
				if  (volEnvLength <= 0) {
					volEnvPos++;

					if ((volEnvPos == volEnvSustain && (volEnvType & 0x2) != 0)) {
						volEnv = currentInstrument.volumeEnvelopePoints[volEnvPos].y;
						volEnvK = currentInstrument.volumeEnvInfo[volEnvPos].y;
						volEnvLength = (int) currentInstrument.volumeEnvInfo[volEnvPos].x;
						sustain = true;
					} else if (volEnvPos == volEnvLoopLen) {
						if ((volEnvType & 0x4) != 0) { // loop
							volEnvPos = currentInstrument.volLoopStart;
							volEnv = currentInstrument.volumeEnvelopePoints[volEnvPos].y;
							volEnvLoopLen = currentInstrument.volLoopEnd;
							volEnvK = currentInstrument.volumeEnvInfo[volEnvPos].y;
							volEnvLength = (int) currentInstrument.volumeEnvInfo[volEnvPos].x;
						} else {
							volEnv = currentInstrument.volumeEnvelopePoints[volEnvPos].y;
							volEnvK = 0;
							useVolEnv = false;
						}
					} else {
						volEnv = currentInstrument.volumeEnvelopePoints[volEnvPos].y;
						volEnvK = currentInstrument.volumeEnvInfo[volEnvPos].y;
						volEnvLength = (int) currentInstrument.volumeEnvInfo[volEnvPos].x;
					}
				}
				volEnvLength--;
			}
		} else if (currentNote == 97) {
			active = false;
			return;
		}
	}

}
/*
 * ChangeLog:
 * $Log: InstrumentManager.java,v $
 * Revision 1.1  2003/08/21 09:25:35  fredde
 * moved instrument-playing from Channel into InstrumentManager
 *
 */
