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
 * @version $Id: Channel.java,v 1.4 2000/10/07 13:48:06 fredde Exp $
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
	int			currentEffect		= 0;
	int			currentEffectParam	= 0;
	double		currentPitch		= 0;
	double		currentPos			= 0;

	boolean useVolEnv = false;
	float volEnvK = 0;
	float volEnv = 64;
	int volEnvLength = 0;
	int volEnvType = 0;
	int volEnvPos = 0;
	int volEnvSustain = 0;

	float finalVol;
	int fadeOutVol;
	

	private final double calcPitch(int note) {
		if (currentInstrument.sample.length == 0) return 0;
		note += currentInstrument.sample[0].relativeNote -1;

		double period = 10*12*16*4 - note*16*4 - currentInstrument.sample[0].fineTune/2;
		double freq = 8363 * Math.pow(2, ((6 * 12 * 16 * 4 - period) / (12 * 16 * 4)));
		double per = 1 / ((double) xm.deviceSampleRate / freq);

		return per;
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

	private final int updateEffects() {
		if (currentEffect == 0) return 0;
		int ret = 0;
		switch (currentEffect) {
			case 0x0D: // pattern break
				// TODO: jump to row specified in the param...
				ret = -1;
				xm.playingPatternPos++;
				xm.playingPattern = xm.patorder[xm.playingPatternPos];
				currentEffect = 0;
				break;
			case 0x0F:	// set tempo
				if (currentEffectParam > 0x20) {
					xm.defaultBpm = currentEffectParam;
					xm.samplesPerTick = (5 * xm.deviceSampleRate) / (2 * xm.defaultBpm);
				} else {
					xm.defaultTempo = currentEffectParam;
					xm.tempo = xm.defaultTempo;
				}
				currentEffect = 0;
				break;
		}
		return ret;
	}

	private final void updateVolumes() {
		finalVol = (((float) currentVolume / 64) * 32);

		finalVol *= (volEnv / 64);

		if (useVolEnv) {
			if (currentNote == 97) {
				fadeOutVol -= currentInstrument.fadeoutVolume;
				if (fadeOutVol <= 0) {
					currentInstrument = null;
					currentNote = 0;
					currentVolume = 64;
					return;
				}

				finalVol *= ((float) fadeOutVol / 65536);

				if (volEnvLength <= 0) {
					volEnvPos += 2;
					volEnv = currentInstrument.volumeEnvelopePoints[volEnvPos + 1];

					if (volEnvPos == currentInstrument.volumeEnvelopePoints.length - 2) {
						volEnvK = 0;
						useVolEnv = false;
					} else {
						volEnvK = calcK();
					}
				}
				volEnvLength--;
			} else if  (!(volEnvPos == (volEnvSustain*2) && (volEnvType & 0x2) != 0) && volEnvLength <= 0) {
				volEnvPos += 2;
				volEnv = currentInstrument.volumeEnvelopePoints[volEnvPos + 1];

				if (volEnvPos == currentInstrument.volumeEnvelopePoints.length - 2) {
					volEnvK = 0;
					useVolEnv = false;
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
		}
	}

	final int update(Pattern pattern, int patternpos) {

		int check = pattern.data[patternpos++];

		if ((check & 0x80) != 0) {
			// note
			if ((check & 0x1) != 0) {
				currentNote = pattern.data[patternpos++];
			}

			// instrument
			if ((check & 0x2) != 0) {
				currentInstrument = xm.instrument[pattern.data[patternpos++] - 1];
				currentPitch = calcPitch(currentNote);
				currentPos = 0;
				currentVolume = 64;
				fadeOutVol	= 65536;

				if (currentInstrument.volumeEnvelopePoints.length != 0) {
					volEnv = currentInstrument.volumeEnvelopePoints[1];
					volEnvPos = 0;
					volEnvK = calcK();
					volEnvSustain = currentInstrument.volSustain;
					volEnvType = currentInstrument.volType;
					useVolEnv = ((volEnvType & 0x1) != 0);
				} else {
					useVolEnv = false;
					volEnv = 64;
					volEnvK = 0;
					volEnvPos = 0;
				}
			}

			// volume
			if ((check & 0x4) != 0) currentVolume = pattern.data[patternpos++];

			// effect
			if ((check & 0x8) != 0) currentEffect = pattern.data[patternpos++];

			// effect param
			if ((check & 0x10) != 0) currentEffectParam = pattern.data[patternpos++];
		} else {
			currentNote			= check;
			currentInstrument	= xm.instrument[pattern.data[patternpos++] - 1];
			currentVolume		= pattern.data[patternpos++];
			currentEffect		= pattern.data[patternpos++];
			currentEffectParam	= pattern.data[patternpos++];
			fadeOutVol			= 65536;

			if (currentInstrument.volumeEnvelopePoints.length != 0) {
				volEnv = currentInstrument.volumeEnvelopePoints[1];
				volEnvPos = 0;
				volEnvK = calcK();
				volEnvSustain = currentInstrument.volSustain;
				volEnvType = currentInstrument.volType;
				useVolEnv = ((volEnvType & 0x1) != 0);
			} else {
				useVolEnv = false;
				volEnv = 64;
				volEnvK = 0;
				volEnvPos = 0;
			}

			currentPitch = calcPitch(currentNote);
			currentPos = 0;
		}
		if (currentEffectParam < 0) currentEffectParam = 256 + currentEffectParam;

		int test = 0;
		test = updateEffects();

		return (test == -1) ? test : patternpos;
	}

	public final void updateTick() {
		updateEffects();
		if (currentInstrument != null) {
			updateVolumes();
		}
	}

	final void play(int[] buffer, int off, int len) {
		if (finalVol < 1 || currentNote == 0 || currentInstrument == null || currentInstrument.sample.length == 0) return;

		for (int i = off; i < off+len; i++) {
			int sample = (int) (currentInstrument.sample[0].sampleData[(int) currentPos] * finalVol);
			buffer[i] += (sample & 65535) | (sample << 16);

			currentPos += currentPitch;
			if ( ((int) currentPos) >= currentInstrument.sample[0].sampleData.length) {
				// pingpong loop
				if ((currentInstrument.sample[0].loopType & 0x2) != 0) {
					currentPitch = -currentPitch;
					currentPos += currentPitch;
				} else if ((currentInstrument.sample[0].loopType & 0x1) != 0) {
					// TODO: should use the samples loopstart and loopend
					currentPos = 0;
				} else {
					currentInstrument = null;
					currentVolume = 64;
					volEnv = 64;
					volEnvK = 0;
					volEnvPos = 0;

					return;
				}
			} else if ( ((int) currentPos) < 0) {
				currentPitch = -currentPitch;
				currentPos += currentPitch;
			}
		}
	}

}
/*
 * ChangeLog:
 * $Log: Channel.java,v $
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
