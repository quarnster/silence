/* Instrument.java - Stores information about an instrument
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
package silence.format.xm.data;

/**
 * This class stores information about an instrument
 *
 * @author Fredrik Ehnbom
 */
public class Instrument {

	private byte[]		sampleForNote;
	private Sample[]	sample;

	private int		fadeoutVolume;

	private Vibrato		vibrato;

	private Envelope	volumeEnvelope	= new Envelope();
	private Envelope	panningEnvelope	= new Envelope();

	public Sample getSampleForNote(int note) {
		int idx = 0;
		if (note-1 >= sampleForNote.length)
			idx = sampleForNote[sampleForNote.length-1];
		else
			idx = sampleForNote[note-1];
		if (idx >= sample.length)
			return null;
		return sample[idx];
	}
	public boolean hasSamples() {
		if (sample == null) return false;
		return sample.length > 0;
	}

	public Envelope	getVolumeEnvelope() { return volumeEnvelope; }
	public void setVolumeEnvelope(Envelope e) { volumeEnvelope = e; }

	public Envelope	getPanningEnvelope() { return panningEnvelope; }
	public void setPanningEnvelope(Envelope e) { panningEnvelope = e; }

	public void setVibrato(Vibrato v) { vibrato = v; }
	public Vibrato getVibrato() { return vibrato; }

	public void setFadeout(int f) { fadeoutVolume = f; }
	public int getFadeout() { return fadeoutVolume; }

	public void setSamples(Sample[] samples) { sample = samples; }
	public void setSampleForNote(byte[] s) { sampleForNote = s; }
}
