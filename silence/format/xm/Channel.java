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
 * @version $Id: Channel.java,v 1.12 2003/08/22 06:51:26 fredde Exp $
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
	int		currentEffect		= -1;
	int		currentEffectParam	= 0;

	int		porta = 0;
	int		portaNote = 0;
	int		portaTarget = 0;



	int timer = 0;
	private final void updateEffects() {
		switch (currentEffect) {
			case 0x00: // Arpeggio
				int tmp = xm.tick % 3;
				switch (tmp) {
					case 0:
						im.setNote(currentNote); break;
					case 1:
						im.setNote(currentNote + ((currentEffectParam >> 4)&0xf)); break;
					case 2:
						im.setNote(currentNote + ((currentEffectParam)&0xf)); break;
				}
				break;
			case 0x01: // Porta up
				if (xm.tick == 0) return;
				porta += currentEffectParam;

				im.currentPitch += currentEffectParam;
				break;
			case 0x02: // Porta down
				if (xm.tick == 0) return;
				porta -= currentEffectParam;

				im.currentPitch -= currentEffectParam;
				break;
/*
			case 0x03: // Porta slide
//				if (xm.tick == 0) return;
				if (im.currentPitch < portaTarget) {
					im.currentPitch += currentEffectParam << 10;

					if (im.currentPitch > portaTarget) {
						im.currentPitch = portaTarget;
						currentEffect = -1;
					}
				} else {
					im.currentPitch -= currentEffectParam << 10;

					if (im.currentPitch < portaTarget) {
						im.currentPitch = portaTarget;
						currentEffect = -1;
					}
				}
//				im.setNote(currentNote + porta);

				if (currentEffect == -1) {
					porta = 0;
					currentNote = portaNote;
					im.setNote(currentNote);
				}
				break;
*/
/*
			case 0x09: // sample offset
				currentEffect = -1;

				setPosition(currentEffectParam << 8);
				break;
*/
			case 0x0A: // Volume slide
				im.currentVolume += (currentEffectParam & 0xF0) != 0 ?
							 (currentEffectParam >> 4) & 0xF :
							-(currentEffectParam & 0xF);

				im.currentVolume = im.currentVolume < 0 ? 0 : im.currentVolume > 64 ? 64 : im.currentVolume;

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
						im.active = false;
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

			case 0x1B: // Multi retrig note (Rxx)
				if (xm.tick == 0) return;
				if (im.active && xm.tick % currentEffectParam == 0) {
					im.trigger();
				}
				break;

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

		int newNote = -1;
		Instrument newInstrument = null;
		int newVolume = -1;
		int newEffect = -1;
		int newEffectParam = -1;

		if ((check & 0x80) != 0) {
			// note
			if ((check & 0x1) != 0)	newNote = pattern.data[patternpos++];

			// instrument
			if ((check & 0x2) != 0) {
				int tmp = pattern.data[patternpos++] - 1;
				if (tmp < xm.instrument.length)
					newInstrument = xm.instrument[tmp];
			}

			// volume
			if ((check & 0x4) != 0) newVolume = pattern.data[patternpos++]&0xff;

			// effect
			if ((check & 0x8) != 0)	newEffect = pattern.data[patternpos++];

			// effect param
			if ((check & 0x10) != 0) newEffectParam = pattern.data[patternpos++]&0xff;
		} else {
			newNote			= check;
			newInstrument		= xm.instrument[pattern.data[patternpos++] - 1];
			newVolume		= pattern.data[patternpos++]&0xff;
			newEffect		= pattern.data[patternpos++];
			newEffectParam		= pattern.data[patternpos++]&0xff;
		}

		if (newInstrument != null) {
			im.setInstrument(newInstrument);
		}

		currentEffect = -1;

		if (newEffect != -1) {
			if (newEffectParam == -1) newEffectParam = 0;

			if (newEffectParam == 0) {
effectLoop:
				switch (newEffect) {
					case 0x00: // Arpeggio
					case 0x08: // Set panning
					case 0x09: // Sample offset
					case 0x0B: // Position jump
					case 0x0C: // Set volume
					case 0x0D: // Pattern break
						currentEffectParam = newEffectParam;
						break;
					case 0xE: // Extended
						int eff = (currentEffectParam >> 4) & 0xF;
						switch (eff) {
							case 0x01: // Fine porta up
							case 0x02: // Fine porta down
							case 0x0A: // Fine volume slide up
							case 0x0B: // Fine volume slide down
								break effectLoop;
							default:
								break;
						}
					case 0xF: // Set tempo/BPM
					case 0x10: // Set global volume (Gxx)
					case 0x15: // Set envelope position (Lxx)
					case 0x1D: // Tremor (Txx)
						currentEffectParam = newEffectParam;
						break;
					default: break;
				}
			} else {
				currentEffectParam = newEffectParam;
			}
/*
			if (newEffect == 0x03) {
				if (newNote != -1 && newNote != 97) {
					portaNote = newNote;
					//portaTarget = newNote - currentNote;
					portaTarget = (int) (im.calcPitch(newNote) * 1024);
				}
//				System.out.println("target: " + portaTarget);
				if (newNote != 97)
					newNote = -1;
//				porta = 0;
//				newEffect = currentEffect = -1;
			}
*/
			currentEffect = newEffect;
		}

		if (newNote != -1) {
			if (newNote == 97) {
				im.release();
			} else {
				porta = 0;
				currentNote = newNote;
				im.playNote(currentNote);
			}
		}


		if (newVolume != -1) {
			if (newVolume <= 0x50) { // volume
				im.currentVolume = newVolume-10;
			} else if (newVolume < 0x70) { // volume slide down
//				currentEffect = 0x0A;
//				currentEffectParam = (newVolume - 0x40);
			} else if (newVolume < 0x80) { // volume slide up
//				currentEffect = 0x0A;
//				currentEffectParam = (newVolume - 0x70);
			} else if (newVolume < 0x90) { // fine volume slide down
				im.currentVolume -= (newVolume - 0x80);
			} else if (newVolume < 0xa0) { // fine volume slide up
				im.currentVolume += (newVolume - 0x90);
			} else if (newVolume < 0xb0) { // vibrato speed
			} else if (newVolume < 0xc0) { // vibrato
			} else if (newVolume < 0xd0) { // set panning
			} else if (newVolume < 0xe0) { // panning slide left
			} else if (newVolume < 0xf0) { // panning slide right
			} else if (newVolume >= 0xf0) { // Tone porta
			}
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
 * Revision 1.12  2003/08/22 06:51:26  fredde
 * 0xx,1xx,2xx,rxx implemented. update() from muhmu2-player
 *
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



