/* Channel.java - Handles a channel
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
 * A class that handles a channel
 *
 * @version $Id: Channel.java,v 1.5 2000/10/08 18:01:57 fredde Exp $
 * @author Fredrik Ehnbom
 */
class Channel {
	private Xm xm;

	public Channel(Xm xm) {
		this.xm = xm;
	}

	int			currentNote			= 0;
	Instrument		currentInstrument	= null;
	int			currentVolume		= 64;
	int			currentEffect		= -1;
	int			currentEffectParam	= 0;
	double		currentPitch		= 0;
	double		currentPos			= 0;
	double		currentLoopLen		= 0;

	boolean		useVolEnv			= false;
	float			volEnvK			= 0;
	float			volEnv			= 64;
	int			volEnvLoopLen		= 0;
	int			volEnvLength		= 0;
	int			volEnvType			= 0;
	int			volEnvPos			= 0;
	int			volEnvSustain		= 0;

	float			rowVol;
	float			finalVol;
	int			fadeOutVol;
	

	private final double calcPitch(int note) {
		if (currentInstrument.sample.length == 0) return 0;
		note += currentInstrument.sample[0].relativeNote -1;

		double period = 10*12*16*4 - note*16*4 - (currentInstrument.sample[0].fineTune >> 1);
		double freq = 8363 * Math.pow(2, ((6 * 12 * 16 * 4 - period) / (12 * 16 * 4)));

		return (1 / ((double) xm.deviceSampleRate / freq));
	}

	private final float calcK() {
		volEnvLength =	(
						currentInstrument.volumeEnvelopePoints[volEnvPos + 2] -
		 				currentInstrument.volumeEnvelopePoints[volEnvPos + 0]
					);

		return
			(float) (
				currentInstrument.volumeEnvelopePoints[volEnvPos + 1] -
				currentInstrument.volumeEnvelopePoints[volEnvPos + 3]
			) /
			(float) (
				currentInstrument.volumeEnvelopePoints[volEnvPos + 0] -
				currentInstrument.volumeEnvelopePoints[volEnvPos + 2]
			);
	}

	private final void updateEffects() {
		if (currentEffect == -1) return;
		switch (currentEffect) {
			case 0x0F:	// set tempo
				if (currentEffectParam > 0x20) {
					xm.defaultBpm = currentEffectParam;
					xm.samplesPerTick = (5 * xm.deviceSampleRate) / (2 * xm.defaultBpm);
				} else {
					xm.defaultTempo = currentEffectParam;
					xm.tempo = xm.defaultTempo;
				}
				currentEffect = -1;
				break;
			case 0x11: // global volume slide
				xm.globalVolume += (currentEffectParam & 0xF0) != 0 ?
						(currentEffectParam >> 4) & 0xF :
						-(currentEffectParam & 0xF);
				break;
			case 0x0E: // extended MOD commands
				int eff = (currentEffectParam >> 4) & 0xF;
				if (eff == 0x0C) { // note cut
					if ((currentEffectParam & 0xF) == 0) {
						currentVolume = 0;
					} else {
						currentEffectParam = (eff << 4) + (currentEffectParam & 0xF) - 1;
					}
				}
		}
	}

	private final void updateVolumes() {
		finalVol = rowVol;

		if (currentNote == 97) {
			fadeOutVol -= currentInstrument.fadeoutVolume;
			if (fadeOutVol <= 10) {
				currentInstrument = null;
				return;
			}
			finalVol *= ((float) fadeOutVol / 65536);
		}

		if (useVolEnv) {
			if (currentNote == 97) {
				if (volEnvLength <= 0) {
					volEnvPos += 2;
					volEnv = currentInstrument.volumeEnvelopePoints[volEnvPos + 1];

					if (volEnvPos == volEnvLoopLen) {
						if ((volEnvType & 0x4) != 0) {
							volEnvPos = currentInstrument.volLoopStart * 2;
							volEnv = currentInstrument.volumeEnvelopePoints[volEnvPos + 1];
							volEnvLoopLen = currentInstrument.volLoopEnd * 2;
							volEnvK = calcK();
						} else {
							volEnvK = 0;
							useVolEnv = false;
							if (volEnv <= 1) {
								currentInstrument = null;
								return;
							}
						}
					} else {
						volEnvK = calcK();
					}
				}
				volEnvLength--;
			} else if  (!(volEnvPos == (volEnvSustain*2) && (volEnvType & 0x2) != 0) && volEnvLength <= 0) {
				volEnvPos += 2;
				volEnv = currentInstrument.volumeEnvelopePoints[volEnvPos + 1];

				if (volEnvPos == volEnvLoopLen) {
					if ((volEnvType & 0x4) != 0) {
						volEnvPos = currentInstrument.volLoopStart * 2;
						volEnv = currentInstrument.volumeEnvelopePoints[volEnvPos + 1];
						volEnvLoopLen = currentInstrument.volLoopEnd * 2;
						volEnvK = calcK();
					} else {
						volEnvK = 0;
						useVolEnv = false;
						if (volEnv <= 1) {
							currentInstrument = null;
							return;
						}
					}
				} else {
					volEnvK = calcK();
				}
			} else if ((volEnvPos == (volEnvSustain*2) && (volEnvType & 0x2) != 0)) {
				volEnvK = 0;
			}

			if (currentNote != 97 && !(volEnvPos == (volEnvSustain*2) && (volEnvType & 0x2) != 0)) {
				volEnvLength--;
			}

			volEnv += volEnvK;
		} else if (currentNote == 97) {
			currentInstrument = null;
			return;
		}
		if (volEnv != 64) finalVol *= (volEnv / 64);
		if (xm.globalVolume != 64) finalVol *= ((double) xm.globalVolume / 64);
	}

	final int skip(Pattern pattern, int patternpos) {
		int check = pattern.data[patternpos++];

		if ((check & 0x80) != 0) {
			if ((check & 0x1) != 0) patternpos++;
			if ((check & 0x2) != 0) patternpos++;
			if ((check & 0x4) != 0) patternpos++;
			if ((check & 0x8) != 0) patternpos++;
			if ((check & 0x10) != 0) patternpos++;
		} else {
			patternpos += 4;
		}

		return patternpos;
	}

	final int update(Pattern pattern, int patternpos) {
		int check = pattern.data[patternpos++];

		if ((check & 0x80) != 0) {
			// note
			if ((check & 0x1) != 0)
				currentNote = pattern.data[patternpos++];

			// instrument
			if ((check & 0x2) != 0) {
				currentInstrument	= xm.instrument[pattern.data[patternpos++] - 1];
				currentLoopLen		= currentInstrument.sample[0].sampleData.length - 1;
				currentPitch		= calcPitch(currentNote);
				currentPos			= 0;
				currentVolume		= 64;
				fadeOutVol			= 65536;

				if (currentInstrument.volumeEnvelopePoints.length != 0) {
					volEnv		= currentInstrument.volumeEnvelopePoints[1];
					volEnvPos		= 0;
					volEnvK		= calcK();
					volEnvSustain	= currentInstrument.volSustain;
					volEnvType		= currentInstrument.volType;
					useVolEnv		= ((volEnvType & 0x1) != 0);
					if ((volEnvType & 0x4) != 0)
						volEnvLoopLen = currentInstrument.volLoopEnd * 2;
					else
						volEnvLoopLen = currentInstrument.volumeEnvelopePoints.length - 2;
				} else {
					useVolEnv	= false;
					volEnv	= 64;
					volEnvK	= 0;
					volEnvPos	= 0;
				}
			}

			// volume
			if ((check & 0x4) != 0)
				currentVolume = pattern.data[patternpos++];

			// effect
			if ((check & 0x8) != 0)
				currentEffect = pattern.data[patternpos++];

			// effect param
			if ((check & 0x10) != 0)
				currentEffectParam = pattern.data[patternpos++];
			else
				currentEffectParam = 0;
		} else {
			currentNote			= check;
			currentInstrument	= xm.instrument[pattern.data[patternpos++] - 1];
			currentLoopLen		= currentInstrument.sample[0].sampleData.length - 1;
			currentVolume		= pattern.data[patternpos++];
			currentEffect		= pattern.data[patternpos++];
			currentEffectParam	= pattern.data[patternpos++];
			fadeOutVol			= 65536;

			if (currentInstrument.volumeEnvelopePoints.length != 0) {
				volEnv		= currentInstrument.volumeEnvelopePoints[1];
				volEnvPos		= 0;
				volEnvK		= calcK();
				volEnvSustain	= currentInstrument.volSustain;
				volEnvType		= currentInstrument.volType;
				if ((volEnvType & 0x4) != 0)
					volEnvLoopLen = currentInstrument.volLoopEnd * 2;
				else
					volEnvLoopLen = currentInstrument.volumeEnvelopePoints.length - 2;
				useVolEnv		= ((volEnvType & 0x1) != 0);
			} else {
				useVolEnv	= false;
				volEnv	= 64;
				volEnvK	= 0;
				volEnvPos	= 0;
			}

			currentPitch = calcPitch(currentNote);
			currentPos = 0;
		}
		if (currentEffectParam < 0) currentEffectParam = 256 + currentEffectParam;

		rowVol = (((float) currentVolume / 64) * 32);

		updateEffects();

		return patternpos;
	}

	public final void updateTick() {
		updateEffects();
		if (currentInstrument != null) {
			updateVolumes();
		}
	}

	// table from "A Programmer's guide to sound", page 347
	final float halfTones[] = {
		0.0F,			1.05946309F,	1.12246205F,	1.18920712F,
		1.25992105F,	1.33483985F,	1.41421356F,	1.49830708F,
		1.58740105F,	1.68179283F,	1.78179744F,	1.88774863F,
		2.0F,			2.11892619F,	2.24492410F,	2.37841423F
	};

	final void play(int[] buffer, int off, int len) {
		if (finalVol < 1 || currentNote == 0 || currentInstrument == null || currentInstrument.sample.length == 0) return;

		for (int i = off; i < off+len; i++) {
			int sample = (int) (currentInstrument.sample[0].sampleData[(int) currentPos] * finalVol);
			buffer[i] += (sample & 65535) | (sample << 16);

			switch (currentEffect) {
			}
			currentPos += currentPitch;
			currentLoopLen += (currentPitch < 0) ? currentPitch : -currentPitch;

			if (currentLoopLen <= 0) {
				if ((currentInstrument.sample[0].loopType & 0x2) != 0) {
					// pingpong loop
					currentPitch = -currentPitch;
					currentPos += currentPitch;
					currentLoopLen = currentInstrument.sample[0].sampleData.length - 1;
				} else if ((currentInstrument.sample[0].loopType & 0x1) != 0) {
					// forward loop
					currentPos = currentInstrument.sample[0].loopStart;
					currentLoopLen = currentInstrument.sample[0].loopEnd;
				} else {
					// no loop
					currentInstrument = null;

					return;
				}
			}
		}
	}
}
/*
 * ChangeLog:
 * $Log: Channel.java,v $
 * Revision 1.5  2000/10/08 18:01:57  fredde
 * changes to play the file even better.
 *
 * Revision 1.4  2000/10/07 13:48:06  fredde
 * Lots of fixes to play correct.
 * Added volume stuff.
 *
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
