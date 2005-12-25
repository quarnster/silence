/* ChannelUpdateData.java - Structure for holding a channels update data
 * Copyright (C) 2001-2005 Fredrik Ehnbom
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
 * Structure for holding a channels update data
 *
 * @author Fredrik Ehnbom
 */
class ChannelUpdateData {
	private int note;
	private int effect;
	private int effectParameter;
	private int volume;
	private Instrument instrument;
	private final Channel channel;

	public int getNote() {	return note; }
	public void setNote(int note) { this.note = note; }

	public int getEffect() { return effect; }
	public void setEffect(int effect) { this.effect = effect; }

	public int getEffectParameter() { return effectParameter; }
	public void setEffectParameter(int e) { this.effectParameter = e; }

	public int getVolume() { return volume; }
	public void setVolume(int volume) { this.volume = volume; }

	public Instrument getInstrument() { return instrument; }
	public void setInstrument(Instrument i) { this.instrument = i; }

	public Channel getChannel() { return channel; }

	public void reset() {
		setNote(-1);
		setInstrument(null);
		setVolume(-1);
		setEffect(-1);
		setEffectParameter(-1);
	}

	public ChannelUpdateData(Channel c) {
		this.channel = c;
		reset();
	}
}
