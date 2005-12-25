/* EffectHandler.java - Handles effects
 * Copyright (C) 2000-2005 Fredrik Ehnbom
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

import silence.format.xm.data.Instrument;

class EffectManager {
	private static final int VOL_SLIDE_UP = 1;
	private static final int VOL_SLIDE_DOWN = 2;

	private static final int IDX_1xy = 0;
	private static final int IDX_2xy = 1;
	private static final int IDX_3xy = 2;
	private static final int IDX_4xy = 3;
	private static final int IDX_5xy = 4;
	private static final int IDX_6xy = 5;
	private static final int IDX_7xy = 6;
	private static final int IDX_9xy = 7; // not documented in xm.txt
	private static final int IDX_Axy = 8;
	private static final int IDX_E1y = 9;
	private static final int IDX_E2y = 10;
	private static final int IDX_EAy = 11;
	private static final int IDX_EBy = 12;
	private static final int IDX_Hxy = 13;
	private static final int IDX_Pxy = 14;
	private static final int IDX_Rxy = 15;

	private int[]	lastEffectParam	= new int[16];

	private InstrumentManager	im;
	private ModulePlayer		mod;

	private int	currentNote		= -1;

	private int	currentEffect		= -1;
	private int	currentEffectParam	= 0;

	private	int	currentVolEffect	= -1;
	private	int	currentVolEffectParam	= -1;

	// for 3xy
	private	int	portaNote		= -1;
	private	int	portaTarget		= 0;

	// for 4xy, 6xy
	private	int	vibPos			= 0;
	private	int	vibRate			= 0;
	private	int	vibDepth		= 0;
	private	int	vibType			= 0;

	// for EDx
	private	int	delayVolume		= 0;
	private	int	delayNote		= 0;
	private	Instrument delayInstrument	= null;

	// for E6x
	private	int	loopCount		= 0;
	private	int	loopRow			= 0;

	public EffectManager(ModulePlayer mod, InstrumentManager im) {
		this.mod = mod;
		this.im = im;
	}

	private void doPorta() {
		if (portaTarget == 0) return;
		boolean done = false;
		if (im.porta < portaTarget) {
			im.porta += lastEffectParam[IDX_3xy] << 2;

			if (im.porta >= portaTarget)
				done = true;
		} else {
			im.porta -= lastEffectParam[IDX_3xy] << 2;

			if (im.porta <= portaTarget)
				done = true;
		}

		if (done) {
			portaTarget = 0;
			im.porta = 0;
			currentNote = portaNote;
			im.setNote(currentNote);
		}
	}

	private void doVibrato() {
		int delta = 0;
		int temp = (vibPos & 31);

		switch (vibType & 3){
		case 0: delta = (int) (Math.abs((Math.sin(vibPos * 2 * Math.PI / 64)) * 256));
			break;
		case 1: temp <<= 3;
			if (vibPos < 0) temp = 255 - temp;
			delta = temp;
			break;
		case 2: 
		case 3: delta = 255;
			break;
		};

		delta *= vibDepth;
		delta >>= 7;
		delta <<= 2;           // we use 4*periods so make vibrato 4 times bigger

		if (vibPos >= 0) im.freqDelta = delta;
		else             im.freqDelta = -delta;

		vibPos += vibRate;
		if (vibPos > 31) vibPos -= 64;
	}

	public void tick() {
		if (currentVolEffect != -1)
			updateVolume();
		if (currentEffect != -1)
			updateEffects();

	}
	private int getVolumeSlide(int effectParameter) {
		return (effectParameter & 0xF0) != 0 ?
				 (effectParameter >> 4) & 0xF : // Slide up
				-(effectParameter & 0xF);	// Slide down
	}

	private void updateEffects() {
		switch (currentEffect) {
			case 0x00: // Arpeggio
			{
				int tmp = mod.getTick() % 3;
				switch (tmp) {
					case 0:
						im.setNote(currentNote); break;
					case 1:
						im.setNote(currentNote + ((currentEffectParam >> 4)&0xf)); break;
					case 2:
						im.setNote(currentNote + ((currentEffectParam)&0xf)); break;
				}
			}
				break;
			case 0x01: // Porta up
				if (mod.getTick() == 0) return;
				im.porta -= lastEffectParam[IDX_1xy] << 2;
				im.calcPitch(currentNote);
				if (im.period < 56) {
					// don't glide too much
					// example: wheresmysauna.xm
					im.porta += 56 - im.period;
					currentEffect = -1;
				}
				break;
			case 0x02: // Porta down
				if (mod.getTick() == 0) return;
				im.porta += lastEffectParam[IDX_2xy] << 2;
				break;
			case 0x03: // Porta slide
				if (mod.getTick() == 0) return;
				doPorta();
				break;
			case 0x04: // Vibrato
				if (mod.getTick() == 0) return;
				vibDepth = (lastEffectParam[IDX_4xy] >> 4) &0xf;
				vibRate = lastEffectParam[IDX_4xy] & 0xf;
				doVibrato();
				break;
			case 0x05: // Porta + volume slide
				if (mod.getTick() == 0) return;
				doPorta();
				im.setVolume(im.getVolume() + getVolumeSlide(lastEffectParam[IDX_5xy]));
				break;
			case 0x06: // Vibrato + volume slide
				if (mod.getTick() == 0) return;
				doVibrato();
				im.setVolume(im.getVolume() + getVolumeSlide(lastEffectParam[IDX_6xy]));
				break;
			case 0x08: // Panning
				im.setPanning(currentEffectParam);
				currentEffect = -1;
				break;
			case 0x09: // sample offset
				currentEffect = -1;

				im.trigger();
				im.setPosition(lastEffectParam[IDX_9xy] << 8);
				break;
			case 0x0A: // Volume slide
				if (mod.getTick() == 0) return;
				im.setVolume(im.getVolume() + getVolumeSlide(lastEffectParam[IDX_Axy]));
				break;
			case 0x0B: // Pattern jump
				mod.setPatternJumpOrder(currentEffectParam);
				currentEffect = -1;
				break;
			case 0x0C: // set volume
				im.setVolume(currentEffectParam);
				currentEffect = -1;
				break;
			case 0x0D: // Pattern break
				mod.setPatternJumpRow(((currentEffectParam>>4) & 0xf) * 10 + (currentEffectParam&0xf));
				currentEffect = -1;
				break;
			case 0x0E: // extended MOD commands
				int eff = (currentEffectParam >> 4) & 0xF;
				int par = currentEffectParam &0xF;
				switch (eff) {
					case 0x01: // fine porta up
						im.porta -= lastEffectParam[IDX_E1y] << 2;
						currentEffect = -1;
						break;
					case 0x02: // fine porta down
						im.porta += lastEffectParam[IDX_E2y] << 2;
						currentEffect = -1;
						break;
					case 0x06: // Pattern loop
						if (par == 0) loopRow = mod.getRow();
						else {
							if (loopCount == 0) loopCount = par;
							else loopCount--;
							if (loopCount > 0) mod.patternJump(mod.getOrder(), loopRow-1);
						}
						currentEffect = -1;
						break;
					case 0x09: // retrig note
						if (mod.getTick() == 0) return;
						if (par == 0) {
							currentEffect = -1;
							return;
						}
						if (im.active && mod.getTick() % par == 0) {
							im.trigger();
						}
						break;
					case 0x0A: // fine volume slide up
						im.setVolume(im.getVolume() + lastEffectParam[IDX_EAy]);
						currentEffect = -1;
						break;
					case 0x0B: // fine volume slide down
						im.setVolume(im.getVolume() - lastEffectParam[IDX_EBy]);
						currentEffect = -1;
						break;
					case 0x0C: // note cut
						if (mod.getTick() == par)
							im.active = false;
						break;
					case 0x0D: // Note delay
						if (mod.getTick() == par) {
							im.setInstrument(delayInstrument);
							im.playNote(delayNote);
							currentNote = delayNote;
							setVolumeEffect(delayVolume);
							currentEffect = -1;
						}
						break;
					case 0x0E: // pattern delay
						mod.setPatternDelay(par);
						currentEffect = -1;
						break;
					default:
						currentEffect = -1;
						System.out.println("Unimplemented E-effect: " + Integer.toHexString(eff));
						break;
				}
				break;
			case 0x0F:	// set tempo
				if (currentEffectParam > 0x20) {
					mod.setBpm(currentEffectParam);
				} else {
					mod.setTempo(currentEffectParam);
				}
				currentEffect = -1;
				break;
			case 0x10: // set global volume (Gxx)
				mod.setGlobalVolume(currentEffectParam);
				currentEffect = -1;
				break;
			case 0x11: // global volume slide (Hxx)
				if (mod.getTick() == 0) return;
				mod.setGlobalVolume(mod.getGlobalVolume() + getVolumeSlide(lastEffectParam[IDX_Hxy]));
				break;
			case 0x1B: // Multi retrig note (Rxx)
				// TODO: volume slide for currentEFfect &0xf0???
				if (mod.getTick() == 0) return;
				if ((lastEffectParam[IDX_Rxy]&0xf) == 0) {
					currentEffect = -1;
					return;
				}
				if (im.active && mod.getTick() % (lastEffectParam[IDX_Rxy]&0xf) == 0) {
					int p = (lastEffectParam[IDX_Rxy]>>4) & 0xf;
					int vol = im.getVolume();
					switch (p) {
					      case 1: im.setVolume(vol-1); break;
					      case 2: im.setVolume(vol-2); break;
					      case 3: im.setVolume(vol-4); break;
					      case 4: im.setVolume(vol-8); break;
					      case 5: im.setVolume(vol-16); break;
					      case 6: im.setVolume((vol*2)/3); break;
					      case 7: im.setVolume(vol/2); break;
					      case 8: break;
					      case 9: im.setVolume(vol+1); break;
					      case 0xa: im.setVolume(vol+2); break;
					      case 0xb: im.setVolume(vol+4); break;
					      case 0xc: im.setVolume(vol+8); break;
					      case 0xd: im.setVolume(vol+16); break;
					      case 0xe: im.setVolume(vol*3/2); break;
					      case 0xf: im.setVolume(vol*2); break;
					}

					im.trigger();
				}
				break;

			default: // unknown effect
				System.out.println("unknown effect: " + Integer.toHexString(currentEffect) + " (" + currentEffect + ")");
				currentEffect = -1;
				break;
		}
	}

	public void updateData(ChannelUpdateData ud) {
		currentEffect = -1;
		int newEffect = ud.getEffect();
		int newEffectParam = ud.getEffectParameter();

		if (newEffectParam == -1) newEffectParam = 0;

		currentEffectParam = newEffectParam;
		if (newEffectParam != 0 && !(newEffect == 0xE && (newEffectParam &0xf) == 0)) {
			// TODO: this could be handled in a better way instead of a switch case..
			switch (newEffect) {
				case 0x01: // porta up
					lastEffectParam[IDX_1xy] = currentEffectParam;
					break;
				case 0x02: // porta down
					lastEffectParam[IDX_2xy] = currentEffectParam;
					break;
				case 0x03: // porta to note
					lastEffectParam[IDX_3xy] = currentEffectParam;
					break;
				case 0x04: // Vibrato
					lastEffectParam[IDX_4xy] = currentEffectParam;
					break;
				case 0x05: // Tone porta + volume slide
					lastEffectParam[IDX_5xy] = currentEffectParam;
					break;
				case 0x06: // Vibrato + volume slide
					lastEffectParam[IDX_6xy] = currentEffectParam;
					break;
				case 0x07: // Tremolo
					lastEffectParam[IDX_7xy] = currentEffectParam;
					break;
				case 0x09: // Sample offset
					lastEffectParam[IDX_9xy] = currentEffectParam;
					break;
				case 0x0A: // volume slide
					lastEffectParam[IDX_Axy] = currentEffectParam;
					break;
				case 0x0E:
					int eff = (currentEffectParam >> 4) & 0xf;
					switch (eff) {
						case 0x1: // fine porta up
							lastEffectParam[IDX_E1y] = currentEffectParam&0xf;
							break;
						case 0x2: // fine porta down
							lastEffectParam[IDX_E2y] = currentEffectParam&0xf;
							break;
						case 0xA: // fine volume slide up
							lastEffectParam[IDX_EAy] = currentEffectParam&0xf;
							break;
						case 0xB: // fine volume slide down
							lastEffectParam[IDX_EBy] = currentEffectParam&0xf;
							break;
					}
					break;
				case 0x11: // Global volume slide
					lastEffectParam[IDX_Hxy] = currentEffectParam;
					break;
				case 0x19: // Panning slide
					lastEffectParam[IDX_Pxy] = currentEffectParam;
					break;
				case 0x1B: // multi retrig
					lastEffectParam[IDX_Rxy] = currentEffectParam;
					break;
			}
		}

		int newNote = ud.getNote();
		if (newEffect == 0x03) {
			if (im.getInstrument() == null || currentNote == -1 || (portaNote == -1 && newNote == -1)) {
				newEffect = -1;
				ud.setInstrument(null);
			}
			else if (newNote != -1 && newNote != 97) {
				im.setNote(currentNote);
				portaNote = newNote;
				portaTarget = (im.getPeriod(portaNote) - im.porta) - im.getPeriod(currentNote);
				ud.setNote(-1);
				newNote = -1;
			}
		} else if (newEffect == 0xE && ((newEffectParam >> 4) & 0xf) == 0xD) {
			// note delay
			delayNote = ud.getNote();
			delayVolume = ud.getVolume();
			delayInstrument = ud.getInstrument();
			ud.setInstrument(null);
			ud.setNote(-1);
			ud.setVolume(0);
		}

		if (newNote != -1 && newNote != 97) {
			vibPos = 0;
			currentNote = newNote;
		}

		if (newEffect == -1 && newEffectParam != 0) newEffect = 0; // Arpeggio
		currentEffect = newEffect;

		setVolumeEffect(ud.getVolume());
	}

	private void updateVolume() {
		switch (currentVolEffect) {
			case VOL_SLIDE_DOWN: // Volume slide
				if (mod.getTick() == 0) return;
				im.setVolume(im.getVolume() - currentVolEffectParam);
				if (im.getVolume() <= 0) {
					currentVolEffect = -1;
				}

				break;
			case VOL_SLIDE_UP: // Volume slide
				if (mod.getTick() == 0) return;
				im.setVolume(im.getVolume() + currentVolEffectParam);
				if (im.getVolume() >= 64) {
					currentVolEffect = -1;
				}

				break;
		}
	}

	private void setVolumeEffect(int newVolume) {
		currentVolEffect = -1;
		if (newVolume < 0x10) return;

		if (newVolume <= 0x50) {
			// Not really a volume effect..
			im.setVolume(newVolume - 0x10);
			return;
		}
		int volEff = newVolume &0xf0;
		currentVolEffectParam = newVolume & 0xf;
		switch (volEff) {
			case 0x60: // Volume slide down
				currentVolEffect = VOL_SLIDE_DOWN;
				break;
			case 0x70: // Volume slide up
				currentVolEffect = VOL_SLIDE_UP;
				break;
			case 0x80: // fine volume slide down
				im.setVolume(im.getVolume() - currentVolEffectParam);
				break;
			case 0x90: // fine volume slide up
				im.setVolume(im.getVolume() + currentVolEffectParam);
				break;
			case 0xa0: // vibrato speed
				vibRate = currentVolEffectParam;
				break;
			case 0xc0: // set panning
				im.setPanning(currentVolEffectParam * 17);
				break;
			default:
				System.out.println("unimplemented volume effect: " + Integer.toHexString(volEff)); break;
//		} else if (newVolume < 0xe0) { // panning slide left
//		} else if (newVolume < 0xf0) { // panning slide right
//		} else if (newVolume >= 0xf0) { // Tone porta
//		} else {
//			System.out.println("unimplemented volume effect: " + newVolume);
		}
	}
}
