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
 * @version $Id: Channel.java,v 1.11 2003/08/21 09:25:35 fredde Exp $
 * @author Fredrik Ehnbom
 */
class Channel {
	private Xm xm;
	private InstrumentManager im;

	public Channel(Xm xm) {
		this.xm = xm;
		im = new InstrumentManager(xm);
	}

	int		currentNote		= 0;
//	Instrument	currentInstrument	= null;
	int		currentVolume		= 64;
	int		currentEffect		= -1;
	int		currentEffectParam	= 0;

//	int		currentPitch		= 0;
//	int		currentPos		= 0;

//	int		currentLoopStart	= 0;
//	int		currentLoopEnd		= 0;


//	float		rowVol;
//	float		finalVol;



	int timer = 0;
	private final void updateEffects() {
		switch (currentEffect) {

			case 0x00: // Arpeggio
				if (timer == 3) break;
				int tmp = timer % 3;
				switch (tmp) {
					case 0:
						im.playNote(currentNote); break;
					case 1:
						im.playNote(currentNote + (currentEffectParam >> 4)&0xf); break;
					case 2:
						im.playNote(currentNote + (currentEffectParam)&0xf); break;
				}
				timer++;
				if (timer == 3) {
					currentEffect = -1;
				}
				break;

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
				im.currentVolume += (currentEffectParam & 0xF0) != 0 ?
							 (currentEffectParam >> 4) & 0xF :
							-(currentEffectParam & 0xF);

				im.currentVolume = currentVolume < 0 ? 0 : currentVolume > 64 ? 64 : currentVolume;

				if (im.currentVolume == 0 && (currentEffectParam & 0xF0) == 0) currentEffect = -1;
				break;
			case 0x0C: // set volume
				im.currentVolume = currentEffectParam;
				currentEffect = -1;

				break;
			case 0x0E: // extended MOD commands
				int eff = (currentEffectParam >> 4) & 0xF;
				if (eff == 0x0C) { // note cut
					if ((currentEffectParam & 0xF) == 0) {
						im.currentVolume = 0;
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
					xm.tick = 0;
				}
				currentEffect = -1;
				break;
			case 0x10: // set global volume (Gxx)
				xm.globalVolume = currentEffectParam;
				currentEffect = -1;
				break;
			case 0x11: // global volume slide (Hxx)
				if (xm.tick <= 1) return;
				if (xm.tick + 1 >= xm.defaultTempo) currentEffect = -1;

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
				System.out.println("unknown: " + currentEffect);
				currentEffect = -1;
				break;
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
			if ((check & 0x1) != 0) {
				currentNote = pattern.data[patternpos++];
				timer = 0;
				if (currentNote == 97) {
					im.release();
				} else {
					im.playNote(currentNote);
				}
			}

			// instrument
			if ((check & 0x2) != 0) {
				currentEffect = -1;
				timer = 0;

				int instr = pattern.data[patternpos++] - 1;
				if (instr >= xm.instrument.length)  im.setInstrument(null);
				else im.setInstrument(xm.instrument[instr]);

				im.playNote(currentNote);
			}

			// volume
			if ((check & 0x4) != 0) {
				int tmp = pattern.data[patternpos++]&0xff;

				if (tmp <= 0x50) { // volume
					im.currentVolume = tmp-1;
				} else if (tmp < 0x70) { // volume slide down
					currentEffect = 0x0A;
					currentEffectParam = (tmp - 0x40);
				} else if (tmp < 0x80) { // volume slide up
					currentEffect = 0x0A;
					currentEffectParam = (tmp - 0x70);

				} else if (tmp < 0x90) { // fine volume slide down
					im.currentVolume -= (tmp - 0x80);
				} else if (tmp < 0xa0) { // fine volume slide up
					im.currentVolume += (tmp - 0x90);
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
			if ((check & 0x10) != 0) {
				currentEffectParam = pattern.data[patternpos++]&0xff;
				if (currentEffect == -1) currentEffect = 0;
			}
		} else {
			currentNote		= check;
			im.setInstrument(xm.instrument[pattern.data[patternpos++] - 1]);
			im.playNote(currentNote);

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
		}

		return patternpos;
	}

	public final void updateTick() {
		if (currentEffect != -1) updateEffects();
		im.updateVolumes();
	}

	final void play(int[] buffer, int off, int len) {
		im.play(buffer, off, len);
	}
}
/*
 * ChangeLog:
 * $Log: Channel.java,v $
 * Revision 1.11  2003/08/21 09:25:35  fredde
 * moved instrument-playing from Channel into InstrumentManager
 *
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



