/* Channel.java - Handles a channel
 * Copyright (C) 2000-2002 Fredrik Ehnbom
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
 * @version $Id: Channel.java,v 1.10 2002/03/20 13:37:25 fredde Exp $
 * @author Fredrik Ehnbom
 */
class Channel {
	private Xm xm;

	public Channel(Xm xm) {
		this.xm = xm;
	}

	int		currentNote		= 0;
	Instrument	currentInstrument	= null;
	int		currentVolume		= 64;
	int		currentEffect		= -1;
	int		currentEffectParam	= 0;

	int		currentPitch		= 0;
	int		currentPos		= 0;

	int		currentLoopStart	= 0;
	int		currentLoopEnd		= 0;

	boolean		useVolEnv		= false;
	boolean		sustain			= false;
	float		volEnvK			= 0;
	float		volEnv			= 64;
	int		volEnvLoopLen		= 0;
	int		volEnvLength		= 0;
	int		volEnvType		= 0;
	int		volEnvPos		= 0;
	int		volEnvSustain		= 0;

	float		rowVol;
	float		finalVol;
	int		fadeOutVol;

	private final float volumeScale = 0.25f;

	private final double calcPitch(int note) {
		if (currentInstrument.sample.length == 0) return 0;
		note += (currentInstrument.sample[0].relativeNote - 1);

		int period = (10*12*16*4) - (note*16*4) - (currentInstrument.sample[0].fineTune / 2);

		double freq = 8363d * Math.pow(2d, ((6d * 12d * 16d * 4d - period) / (double) (12 * 16 * 4)));
		double pitch = (freq / (double) xm.deviceSampleRate);

		return  pitch;
	}

	private final void updateEffects() {
		switch (currentEffect) {
/*
			case 0x01: // Porta up
				if (qxm.tick == 0) return;
				porta -= currentEffectParam << 2;

				if (porta < -856*4) {
					porta = -856*4;
					currentEffect = -1;
				}
				setPitch(calcPitch(currentNote));
				break;
			case 0x02: // Porta down
				if (qxm.tick == 0) return;
				porta += currentEffectParam << 2;

				if (porta > 856*4) {
					porta = 856*4;
					currentEffect = -1;
				}
				setPitch(calcPitch(currentNote));
				break;
			case 0x03: // Porta slide

				if (porta < portaTarget) {
					porta += currentEffectParam << 2;

					if (porta > portaTarget) {
						porta = portaTarget;
						currentEffect = -1;
					}
				} else {
					porta -= currentEffectParam << 2;

					if (porta < portaTarget) {
						porta = portaTarget;
						currentEffect = -1;
					}
				}

				if (currentEffect == -1) {
					porta = 0;
					currentNote = portaNote;
					setPitch(calcPitch(currentNote));
				}
				break;
			case 0x09: // sample offset
				currentEffect = -1;

				setPosition(currentEffectParam << 8);
				break;
*/
			case 0x0A: // Volume slide
				currentVolume += (currentEffectParam & 0xF0) != 0 ?
							 (currentEffectParam >> 4) & 0xF :
							-(currentEffectParam & 0xF);

				currentVolume = currentVolume < 0 ? 0 : currentVolume > 64 ? 64 : currentVolume;
				rowVol = (( currentVolume / 64f) * volumeScale);

				if (currentInstrument != null && currentInstrument.sample.length > 0) rowVol *= (currentInstrument.sample[0].volume / 64f);

				if (currentVolume == 0 && (currentEffectParam & 0xF0) == 0) currentEffect = -1;
				break;
			case 0x0C: // set volume
				currentVolume = currentEffectParam;
				currentEffect = -1;
				rowVol = (((float) currentVolume / 64) * volumeScale);
				if (currentInstrument != null) rowVol *= ((float) currentInstrument.sample[0].volume / 64);

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
				break;
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
			case 0x10: // set global volume (Gxx)
				xm.globalVolume = currentEffectParam;
				currentEffect = -1;
				break;
			case 0x11: // global volume slide (Hxx)
				if (xm.tempo + 1 == xm.defaultTempo) return;
				if (xm.tempo <= 1) currentEffect = -1;

				xm.globalVolume += (currentEffectParam & 0xF0) != 0 ?
						(currentEffectParam >> 4) & 0xF :
						-(currentEffectParam & 0xF);
				break;
/*
			case 0x1B: // Multi retrig note (Rxx)
				if (qxm.tick == 0) return;
				if (qxm.tick % currentEffectParam == 0) {
					this.trigger();
				}
				break;
*/
			default: // unknown effect
				currentEffect = -1;
				break;
		}
	}

	private final void updateVolumes() {
		finalVol = rowVol;

		if (currentNote == 97) {
			finalVol *= ((float) fadeOutVol / 65536);
			fadeOutVol -= currentInstrument.fadeoutVolume;
			if (fadeOutVol <= 10) {
				currentInstrument = null;
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
								currentInstrument = null;
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
			currentInstrument = null;
			return;
		}
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
				currentEffect = -1;
				currentInstrument	= xm.instrument[pattern.data[patternpos++] - 1];
				currentLoopEnd		= (currentInstrument.sample[0].sampleData.length - 1)<<10;
				currentLoopStart	= (currentInstrument.sample[0].loopStart) << 10;
				double tp		= calcPitch(currentNote);
				currentPitch		= (int) (tp * 1024);
				currentPos		= 0;
				currentVolume		= 64;
				fadeOutVol			= 65536;

				if (currentInstrument.volumeEnvelopePoints.length != 0) {
					volEnv		= currentInstrument.volumeEnvelopePoints[0].y;
					volEnvPos		= 0;
					volEnvK		= currentInstrument.volumeEnvInfo[volEnvPos].y;
					volEnvLength	= (int) currentInstrument.volumeEnvInfo[volEnvPos].x;
					volEnvSustain	= currentInstrument.volSustain;
					volEnvType		= currentInstrument.volType;
					useVolEnv		= ((volEnvType & 0x1) != 0);

					if ((volEnvType & 0x4) != 0)
						volEnvLoopLen = currentInstrument.volLoopEnd;
					else
						volEnvLoopLen = currentInstrument.volumeEnvelopePoints.length - 1;
					sustain = (volEnvPos == volEnvSustain && (volEnvType & 0x2) != 0);
				} else {
					useVolEnv	= false;
					volEnv	= 64;
					volEnvK	= 0;
					volEnvPos	= 0;
				}
				if (currentInstrument.sample.length == 0) currentInstrument = null;
			}

			// volume
			if ((check & 0x4) != 0) {
				int tmp = pattern.data[patternpos++]&0xff;

				if (tmp <= 0x50) { // volume
					currentVolume = tmp-1;
				} else if (tmp < 0x70) { // volume slide down
					currentEffect = 0x0A;
					currentEffectParam = (tmp - 0x40);
				} else if (tmp < 0x80) { // volume slide up
					currentEffect = 0x0A;
					currentEffectParam = (tmp - 0x70);

				} else if (tmp < 0x90) { // fine volume slide down
					currentVolume -= (tmp - 0x80);
				} else if (tmp < 0xa0) { // fine volume slide up
					currentVolume += (tmp - 0x90);
				} else if (tmp < 0xb0) { // vibrato speed
				} else if (tmp < 0xc0) { // vibrato
				} else if (tmp < 0xd0) { // set panning
				} else if (tmp < 0xe0) { // panning slide left
				} else if (tmp < 0xf0) { // panning slide right
				} else if (tmp >= 0xf0) { // Tone porta
				}
			}

			// effect
			if ((check & 0x8) != 0) {
				currentEffect = pattern.data[patternpos++];
				currentEffectParam = 0;
			}

			// effect param
			if ((check & 0x10) != 0)
				currentEffectParam = pattern.data[patternpos++]&0xff;
		} else {
			currentNote		= check;
			currentInstrument	= xm.instrument[pattern.data[patternpos++] - 1];
			currentLoopEnd		= (currentInstrument.sample[0].sampleData.length - 1)<<10;
			currentLoopStart	= (currentInstrument.sample[0].loopStart) << 10;
			currentVolume		= 64;

			int tmp = pattern.data[patternpos++]&0xff;
			if (tmp <= 0x50) { // volume
				currentVolume = tmp;
			} else if (tmp < 0x70) { // volume slide down
				currentEffect = 0x0A;
				currentEffectParam = (tmp - 0x60);
			} else if (tmp < 0x80) { // volume slide up
				currentEffect = 0x0A;
				currentEffectParam = (tmp - 0x70);
			} else if (tmp < 0x90) { // fine volume slide down
				currentVolume -= (tmp - 0x80);
			} else if (tmp < 0xa0) { // fine volume slide up
				currentVolume += (tmp - 0x90);
			} else if (tmp < 0xb0) { // vibrato speed
			} else if (tmp < 0xc0) { // vibrato
			} else if (tmp < 0xd0) { // set panning
			} else if (tmp < 0xe0) { // panning slide left
			} else if (tmp < 0xf0) { // panning slide right
			} else if (tmp >= 0xf0) { // Tone porta
			}
			currentEffect		= pattern.data[patternpos++];
			currentEffectParam	= pattern.data[patternpos++]&0xff;
			fadeOutVol			= 65536;

			if (currentInstrument.volumeEnvelopePoints.length != 0) {
				volEnv		= currentInstrument.volumeEnvelopePoints[0].y;
				volEnvPos		= 0;
				volEnvK		= currentInstrument.volumeEnvInfo[volEnvPos].y;
				volEnvLength	= (int) currentInstrument.volumeEnvInfo[volEnvPos].x;
				volEnvSustain	= currentInstrument.volSustain;
				volEnvType		= currentInstrument.volType;

				if ((volEnvType & 0x4) != 0)
					volEnvLoopLen = currentInstrument.volLoopEnd;
				else
					volEnvLoopLen = currentInstrument.volumeEnvelopePoints.length - 1;
				useVolEnv		= ((volEnvType & 0x1) != 0);
				sustain = (volEnvPos == volEnvSustain && (volEnvType & 0x2) != 0);
			} else {
				useVolEnv	= false;
				volEnv	= 64;
				volEnvK	= 0;
				volEnvPos	= 0;
			}

			double tp 	= calcPitch(currentNote);
			currentPitch	= (int) (tp *1024);
			currentPos	= 0;
		}

		rowVol = (( currentVolume / 64f) * volumeScale);
		if (currentInstrument != null) rowVol *= (currentInstrument.sample[0].volume / 64f);

		return patternpos;
	}

	public final void updateTick() {
		if (currentEffect != -1) updateEffects();
		if (currentInstrument != null) updateVolumes();
	}

	final void play(int[] buffer, int off, int len) {
		if (currentInstrument == null || finalVol < 0.01 || currentNote == 0) return;

		for (int i = off; i < off+len; i++) {
			if ((currentPos >> 10) >=  currentInstrument.sample[0].sampleData.length) {
				System.out.println("currentPos: " + (currentPos>>10) + ", length: " + currentInstrument.sample[0].sampleData.length + ", loop: " + (currentLoopEnd>>10) + ", currentPitch:  " + currentPitch + ", currentLoopStart: " + (currentLoopStart>>10));
			}
			int sample = (int) (currentInstrument.sample[0].sampleData[(int) (currentPos>>10)] * finalVol);
			short right = (short) ((buffer[i] >> 16)&0xffff);
			short left  = (short) (buffer[i] & 0xffff);

			buffer[i] += sample << 16 | sample;
			

			currentPos += currentPitch;

			if (currentPos < currentLoopStart) {
				if (currentPitch < 0) {
					currentPitch = - currentPitch;
					currentPos = currentLoopStart - currentPos;
					currentPos += currentLoopStart;
				}
			} else if (currentPos >= currentLoopEnd) {
				if ((currentInstrument.sample[0].loopType & 0x2) != 0) {
					// pingpong loop
					currentPitch = -currentPitch;

					currentPos = currentLoopEnd - currentPos;
					currentPos += currentLoopEnd;
					if (currentPos < currentLoopStart) currentPos = currentLoopStart;

					currentLoopEnd = currentLoopStart + (currentInstrument.sample[0].loopEnd << 10);
				} else if ((currentInstrument.sample[0].loopType & 0x1) != 0) {
					// forward loop
					currentPos += currentLoopStart - currentLoopEnd;

					if (currentPos < currentLoopStart) currentPos = currentLoopStart;
					currentLoopEnd = currentLoopStart + (currentInstrument.sample[0].loopEnd << 10);
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
 * Revision 1.10  2002/03/20 13:37:25  fredde
 * whoa! lots of changes!
 * among others:
 * * fixed looping (so that some chiptunes does not play false anymore :))
 * * pitch, currentPos and some more stuff now uses fixedpoint maths
 * * added a volumeScale variable for easier changing of the volumescale
 * * a couple of effects that I had implemented in my xm-player for muhmuaudio 0.2
 *   have been copied and pasted into the file. they are commented out though
 *
 * Revision 1.9  2001/01/04 18:55:59  fredde
 * some smaller changes
 *
 * Revision 1.8  2000/12/21 17:19:59  fredde
 * volumeenvelopes works better, uses precalced k-values,
 * pingpong loop fixed
 *
 * Revision 1.7  2000/10/14 19:09:04  fredde
 * changed volume stuff back to 32 since
 * sampleData is of type byte[] again
 *
 * Revision 1.6  2000/10/12 15:04:42  fredde
 * fixed volume envelopes after sustain.
 * updated volumes to work with (8-bit sample) << 8
 *
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



