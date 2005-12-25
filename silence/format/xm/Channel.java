/* Channel.java - Handles a channel
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

import silence.format.xm.data.*;

/**
 * A class that handles a channel
 *
 * @author Fredrik Ehnbom
 */
class Channel {
	ModulePlayer		mod;
	InstrumentManager	im;
	EffectManager		em;
	int			id;

	private static int count = 0;

	public Channel(ModulePlayer mod) {
		count ++;
		id = count;
		this.mod = mod;
		im = new InstrumentManager(mod);
		em = new EffectManager(mod, im);
	}

	final int skip(Pattern pattern, int patternpos) {
		int[] data = pattern.getData();
		if (data.length == 0) return patternpos;
		int check = data[patternpos++];

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

	private ChannelUpdateData ud = new ChannelUpdateData(this);
	private int updateData(Pattern pattern, int patternpos) {
		int[] data = pattern.getData();
		if (data.length == 0) return patternpos;
		int check = data[patternpos++];

		ud.reset();

		if ((check & 0x80) != 0) {
			// note
			if ((check & 0x1) != 0) ud.setNote(data[patternpos++]);

			// instrument
			if ((check & 0x2) != 0) {
				int tmp = data[patternpos++] - 1;
				ud.setInstrument(mod.getModule().getInstrument(tmp));
			}

			// volume
			if ((check & 0x4) != 0) ud.setVolume(data[patternpos++]&0xff);

			// effect
			if ((check & 0x8) != 0)	ud.setEffect(data[patternpos++]);

			// effect param
			if ((check & 0x10) != 0) ud.setEffectParameter(data[patternpos++]&0xff);
		} else {
			ud.setNote(check);
			ud.setInstrument(mod.getModule().getInstrument(data[patternpos++] - 1));
			ud.setVolume(data[patternpos++]&0xff);
			ud.setEffect(data[patternpos++]);
			ud.setEffectParameter(data[patternpos++]&0xff);
		}
		return patternpos;
	}

	final int update(Pattern pattern, int patternpos) {
		patternpos = updateData(pattern, patternpos);

		em.updateData(ud);
		im.updateData(ud);
		return patternpos;
	}

	public final void updateTick() {
		em.tick();
		im.tick();
	}

	final void play(int[] left, int[] right, int off, int len) {
		im.play(left, right, off, len);
	}
}
