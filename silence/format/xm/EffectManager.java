/* $Id: EffectManager.java,v 1.4 2003/09/01 09:06:00 fredde Exp $
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

class EffectManager {
	private static final int VOL_SLIDE_UP = 1;
	private static final int VOL_SLIDE_DOWN = 2;

	private static final int IDX_1xy = 0;
	private static final int IDX_2xy = 1;
	private static final int IDX_3xy = 2;
	private static final int IDX_Axy = 3;
	private static final int IDX_E1y = 4;
	private static final int IDX_E2y = 5;
	private static final int IDX_EAy = 6;
	private static final int IDX_EBy = 7;
	private static final int IDX_Hxy = 8;
	private static final int IDX_Rxy = 9;

	private int[]	lastEffectParam	= new int[10];

	Channel c			= null;

	int	currentEffect		= -1;
	int	currentEffectParam	= 0;

	int	currentVolEffect	= -1;
	int	currentVolEffectParam	= -1;

	int	portaNote		= 0;
	int	portaTarget		= 0;

	int	vibPos			= 0;
	int	vibRate			= 0;
	int	vibDepth		= 0;
	int	vibType			= 0;

	int	delayNote		= 0;

	public EffectManager(Channel c) {
		this.c = c;
	}

	void doVibrato() {
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

		if (vibPos >= 0) c.im.freqDelta = delta;
		else             c.im.freqDelta = -delta;

		vibPos += vibRate;
		if (vibPos > 31) vibPos -= 64;
	}

	final void updateEffects() {
		if (currentVolEffect != -1)
			updateVolume();

		if (currentEffect == -1)
			return;

		switch (currentEffect) {
			case 0x00: // Arpeggio
			{
				int tmp = c.xm.tick % 3;
				switch (tmp) {
					case 0:
						c.im.setNote(c.currentNote); break;
					case 1:
						c.im.setNote(c.currentNote + ((currentEffectParam >> 4)&0xf)); break;
					case 2:
						c.im.setNote(c.currentNote + ((currentEffectParam)&0xf)); break;
				}
			}
				break;
			case 0x01: // Porta up
				if (c.xm.tick == 0) return;
				c.im.porta -= lastEffectParam[IDX_1xy] << 2;
				break;
			case 0x02: // Porta down
				if (c.xm.tick == 0) return;
				c.im.porta += lastEffectParam[IDX_2xy] << 2;
				break;
			case 0x03: // Porta slide
				if (c.xm.tick == 0) return;
				if (c.im.porta < portaTarget) {
					c.im.porta += lastEffectParam[IDX_3xy] << 2;

					if (c.im.porta >= portaTarget) {
						currentEffect = -1;
					}
				} else {
					c.im.porta -= lastEffectParam[IDX_3xy] << 2;

					if (c.im.porta <= portaTarget) {
						currentEffect = -1;
					}
				}

				if (currentEffect == -1) {
					portaTarget = 0;
					c.im.porta = 0;
					c.currentNote = portaNote;
				}
				c.im.setNote(c.currentNote);
				break;

			case 0x04: // Vibrato
				if (c.xm.tick == 0) return;
				vibDepth = (currentEffectParam >> 4) &0xf;
				vibRate = currentEffectParam & 0xf;
				doVibrato();
				break;
			case 0x09: // sample offset
				currentEffect = -1;

				if (currentEffectParam != 0) {
					c.im.trigger();
					c.im.setPosition(currentEffectParam << 8);
				}
				break;

			case 0x0A: // Volume slide
				if (c.xm.tick == 0) return;
				c.im.currentVolume += (lastEffectParam[IDX_Axy] & 0xF0) != 0 ?
							 (lastEffectParam[IDX_Axy] >> 4) & 0xF :
							-(lastEffectParam[IDX_Axy] & 0xF);

				c.im.currentVolume = c.im.currentVolume < 0 ? 0 : c.im.currentVolume > 64 ? 64 : c.im.currentVolume;

//				if (c.im.currentVolume == 0 && (currentEffectParam & 0xF0) == 0) currentEffect = -1;
				break;
			case 0x0C: // set volume
				c.im.currentVolume = currentEffectParam;
				currentEffect = -1;
				break;
			case 0x0E: // extended MOD commands
				int eff = (currentEffectParam >> 4) & 0xF;
				int par = currentEffectParam &0xF;
				if (eff == 0x01) { // fine porta up
					c.im.porta -= lastEffectParam[IDX_E1y] << 2;
					currentEffect = -1;
				} else if (eff == 0x02) { // fine porta down
					c.im.porta += lastEffectParam[IDX_E2y] << 2;
					currentEffect = -1;
				} else if (eff == 0x09) { // retrig note
					if (c.xm.tick == 0) return;
					if (par == 0) {
						currentEffect = -1;
						return;
					}
					if (c.im.active && c.xm.tick % par == 0) {
						c.im.trigger();
					}
				} else if (eff == 0x0A) { // fine volume slide up
					c.im.currentVolume += lastEffectParam[IDX_EAy];
					c.im.currentVolume = c.im.currentVolume < 0 ? 0 : c.im.currentVolume > 64 ? 64 : c.im.currentVolume;
					currentEffect = -1;
				} else if (eff == 0x0B) { // fine volume slide down
					c.im.currentVolume -= lastEffectParam[IDX_EBy];
					c.im.currentVolume = c.im.currentVolume < 0 ? 0 : c.im.currentVolume > 64 ? 64 : c.im.currentVolume;
					currentEffect = -1;
				} else if (eff == 0x0C) { // note cut
					if (c.xm.tick == par)
						c.im.active = false;
				} else if (eff == 0x0E) { // pattern delay
					c.xm.patdelay = par;
					currentEffect = -1;
				} else System.out.println("Unimplemented E-effect: " + eff);
				break;
			case 0x0F:	// set tempo
				if (currentEffectParam > 0x20) {
					c.xm.defaultBpm = currentEffectParam;
					c.xm.samplesPerTick = (5 * c.xm.deviceSampleRate) / (2 * c.xm.defaultBpm);
				} else {
					c.xm.defaultTempo = currentEffectParam;
					c.xm.tick = 0;
				}
				currentEffect = -1;
				break;
			case 0x10: // set global volume (Gxx)
				c.xm.globalVolume = currentEffectParam;
				if (c.xm.globalVolume > 64) c.xm.globalVolume = 64;
				currentEffect = -1;
				break;
			case 0x11: // global volume slide (Hxx)
				if (c.xm.tick == 0) return;
				c.xm.globalVolume += (lastEffectParam[IDX_Hxy] & 0xF0) != 0 ?
						 (lastEffectParam[IDX_Hxy] >> 4) & 0xF :
						-(lastEffectParam[IDX_Hxy] & 0xF);
				c.xm.globalVolume = c.xm.globalVolume > 64 ? 64 : c.xm.globalVolume < 0 ? 0 : c.xm.globalVolume;
				break;

			case 0x1B: // Multi retrig note (Rxx)
				// TODO: volume slide for currentEFfect &0xf0???
				if (c.xm.tick == 0) return;
				if ((lastEffectParam[IDX_Rxy]&0xf) == 0) {
					currentEffect = -1;
					return;
				}
				if (c.im.active && c.xm.tick % (lastEffectParam[IDX_Rxy]&0xf) == 0) {
					c.im.trigger();
				}
				break;

			default: // unknown effect
				System.out.println("unknown effect: " + currentEffect);
				currentEffect = -1;
				break;
		}
	}

	public int setEffect(int newEffect, int newEffectParam, int newNote) {
		if (newEffectParam == -1) newEffectParam = 0;

		if (newEffect == 0xE && (newEffectParam &0xf) == 0) {
			int eff = (currentEffectParam >> 4) & 0xF;
			switch (eff) {
				case 0x01: // Fine porta up
				case 0x02: // Fine porta down
				case 0x0A: // Fine volume slide up
				case 0x0B: // Fine volume slide down
					break;
				default:
					currentEffectParam = newEffectParam;
					break;
			}
		} else if (newEffectParam == 0) {
			switch (newEffect) {
				case 0x00: // Arpeggio
				case 0x08: // Set panning
				case 0x09: // Sample offset
				case 0x0B: // Position jump
				case 0x0C: // Set volume
				case 0x0D: // Pattern break
					currentEffectParam = newEffectParam;
					break;
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
				case 0x1B: // multi retrig
					lastEffectParam[IDX_Rxy] = currentEffectParam;
					break;
			}
		}

		if (newEffect == 0x03) {
			if (newNote != -1 && newNote != 97) {
				portaNote = newNote;
				portaTarget = c.im.getPeriod(portaNote) - c.im.getPeriod(c.currentNote);
				if (c.im.release) {
					c.im.setNote(c.currentNote);
					c.im.trigger();
				}
				c.im.porta = 0;
				c.im.active = true;
				newNote = -1;
			}
		}

		if (newNote != -1) {
			vibPos = 0;
		}

		if (newEffect == -1 && newEffectParam != 0) newEffect = 0;
		currentEffect = newEffect;

		return newNote;
	}

	public void updateVolume() {
		switch (currentVolEffect) {
			case VOL_SLIDE_DOWN: // Volume slide
				c.im.currentVolume -= currentVolEffectParam;
				if (c.im.currentVolume < 0) {
					c.im.currentVolume = 0;
					currentVolEffect = -1;
				}

				break;
			case VOL_SLIDE_UP: // Volume slide
				c.im.currentVolume += currentVolEffectParam;
				if (c.im.currentVolume > 64) {
					c.im.currentVolume = 64;
					currentVolEffect = -1;
				}

				break;
		}
	}

	public void setVolume(int newVolume) {
		currentVolEffect = -1;
		if (newVolume < 0x10) return;
		if (newVolume <= 0x50) { // volume
			c.im.currentVolume = newVolume- 0x10;
		} else if (newVolume < 0x70) { // volume slide down
			currentVolEffect = VOL_SLIDE_DOWN;
			currentVolEffectParam = (newVolume - 0x60);
		} else if (newVolume < 0x80) { // volume slide up
			currentVolEffect = VOL_SLIDE_UP;
			currentVolEffectParam = (newVolume - 0x70);
		} else if (newVolume < 0x90) { // fine volume slide down
			c.im.currentVolume -= (newVolume - 0x80);
		} else if (newVolume < 0xa0) { // fine volume slide up
			c.im.currentVolume += (newVolume - 0x90);
//		} else if (newVolume < 0xb0) { // vibrato speed
//		} else if (newVolume < 0xc0) { // vibrato
//		} else if (newVolume < 0xd0) { // set panning
//		} else if (newVolume < 0xe0) { // panning slide left
//		} else if (newVolume < 0xf0) { // panning slide right
//		} else if (newVolume >= 0xf0) { // Tone porta
//		} else {
//			System.out.println("unimplemented volume effect: " + newVolume);
		}
	}
}

/*
 * $Log: EffectManager.java,v $
 * Revision 1.4  2003/09/01 09:06:00  fredde
 * 00-params, vibrato, Exy-effects etc
 *
 * Revision 1.3  2003/08/23 13:45:39  fredde
 * more 3xx fixes
 *
 * Revision 1.2  2003/08/23 07:40:26  fredde
 * porta effects now working. 9xx implemented
 *
 * Revision 1.1  2003/08/22 12:35:22  fredde
 * moved effects from Channel to EffectManager
 *
 */

