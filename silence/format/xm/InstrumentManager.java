/* $Id: InstrumentManager.java,v 1.1 2005/12/25 21:56:09 quarn Exp $
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
 * This class handles the playing of an instrument
 *
 * @author Fredrik Ehnbom
 */
class InstrumentManager {
	ModulePlayer	mod;
	int		fadeOutVol;

	EnvelopeHandler volumeEnvelope		= new EnvelopeHandler();
	EnvelopeHandler	panningEnvelope		= new EnvelopeHandler();

	private Instrument currentInstrument = null;

	private	SamplePlayer samplePlayer = new SamplePlayer();

	public void setVolume(int vol) {
		if (vol < 0) vol = 0;
		else if (vol > 0x40) vol = 0x40;
		currentVolume = vol;
	}
	public int getVolume() {
		return currentVolume;
	}
	private int currentVolume;
	private float finalVol = 0;

	private float finalPan = 32;

	private int	currentNote = -1;

	private int	panning = 32;

	public void setPanning(int pan) {
		this.panning = pan;
	}
	public int getPanning() {
		return panning;
	}

	private double	freq		= 0;
	int	freqDelta	= 0;
	int	period		= 0;

	private final float volumeScale = 0.5f;

	boolean active = false;
	boolean release = false;
	int porta = 0;

	public InstrumentManager(ModulePlayer mod) {
		this.mod = mod;
	}

	public void setInstrument(Instrument instrument) {
		if (instrument == null || !instrument.hasSamples()) {
			currentInstrument = null;
		} else {
			currentInstrument = instrument;
			volumeEnvelope.setEnvelope(instrument.getVolumeEnvelope());
			panningEnvelope.setEnvelope(instrument.getPanningEnvelope());
			samplePlayer.setPosition(0);
		}
	}
	public Instrument getInstrument() {
		return currentInstrument;
	}

	private final static int[] PeriodTab = new int[]{
		907,900,894,887,881,875,868,862,856,850,844,838,832,826,820,814,
		808,802,796,791,785,779,774,768,762,757,752,746,741,736,730,725,
		720,715,709,704,699,694,689,684,678,675,670,665,660,655,651,646,
		640,636,632,628,623,619,614,610,604,601,597,592,588,584,580,575,
		570,567,563,559,555,551,547,543,538,535,532,528,524,520,516,513,
		508,505,502,498,494,491,487,484,480,477,474,470,467,463,460,457
	};

	final int getPeriodAmiga(int note) {
		if (currentInstrument == null || !currentInstrument.hasSamples()) return 0;
		Sample sample = currentInstrument.getSampleForNote(note);
		int finetune = sample.getFineTune();
		note += sample.getRelativeNote();

		int idx1 = (note % 12) * 8 + (finetune / 16);
		if (idx1 < 0) idx1 = 0;
		int idx2 = (note % 12) * 8 + (finetune / 16) + 1;
		if (idx2 < 0) idx2 = 0;

		int frac = (int) (((finetune / 16.0f) - (finetune >> 4)) * 1024);
		int octave = note / 12;

		return (int) ((
			(
				((PeriodTab[idx1] * (1024 - frac)) >> 10) +
				((PeriodTab[idx2] * frac) >> 10)
			) * 32) >> octave)
			+ porta;
	}

	final double getFreqAmiga(int note) {
		period = getPeriodAmiga(note);
		return 8363.0 * 1712.0 / period;
	}

	final int getPeriodLinear(int note) {
		if (currentInstrument == null || !currentInstrument.hasSamples()) return 0;
		Sample sample = currentInstrument.getSampleForNote(note);

		note += (sample.getRelativeNote() - 1);
		return  (10*12*16*4) - (note*16*4) - (sample.getFineTune() / 2) + porta;
	}
	final double getFreqLinear(int note) {
		period = getPeriodLinear(note);
		return 8363d * Math.pow(2d, ((6d * 12d * 16d * 4d - period) / (double) (12 * 16 * 4)));
	}

	public double getFreq(int note) {
		if (mod.getModule().getAmigaFreqTable())
			return getFreqAmiga(note);
		return getFreqLinear(note);
	}
	public int getPeriod(int note) {
		if (mod.getModule().getAmigaFreqTable())
			return getPeriodAmiga(note);
		return getPeriodLinear(note);
	}
	final double calcPitch(int note) {
		if (mod.getModule().getAmigaFreqTable())
			getFreqAmiga(note);
		else
			getFreqLinear(note);

		double pitch = (freq / (double) mod.getDeviceSampleRate());

		return  pitch;
	}

	public void release() {
		release = true;
		volumeEnvelope.release();
		panningEnvelope.release();
	}

	public void setNote(int note) {
		if (currentInstrument == null) return;
		currentNote = note;
		samplePlayer.setSample(currentInstrument.getSampleForNote(currentNote));
	}

	public void playNote(int note) {
		if (currentInstrument == null) return;
		setNote(note);
		trigger();
	}

	public void setPosition(int position) {
		if (currentInstrument != null && position < currentInstrument.getSampleForNote(currentNote).getLength()) {
			samplePlayer.setPosition(position);
		}
	}

	public void trigger() {
		if (currentInstrument == null) return;
		vibPos = 0;
		vibSweepPos = 0;
		porta = 0;
		samplePlayer.setPosition(0);
		samplePlayer.setPitch(0); // just to reset pingpong
	}

	public void play(int[] left, int[] right, int off, int len) {
		if (!active || currentInstrument == null/* || currentNote == 0 || finalVol < 0.01*/) return;

		active = samplePlayer.play(left, right, off, len);
	}

	public void updateData(ChannelUpdateData ud) {
		Instrument newInstrument = ud.getInstrument();
		if (newInstrument != null && !newInstrument.hasSamples()) {
			newInstrument = null;
			ud.setNote(-1);
			active = false;
		}

		int newNote = ud.getNote();
		if (newNote != -1) {
			if (newNote == 97) {
				release();
			} else {
				if (newInstrument != null)
					setInstrument(newInstrument);
				if (currentInstrument != null) {
					playNote(newNote);
				}
			}
		}
		if (newInstrument != null && currentNote != -1) {
			if (newInstrument != currentInstrument) {
				setInstrument(newInstrument);
			}
			setVolume(newInstrument.getSampleForNote(currentNote).getVolume());
			setPanning(newInstrument.getSampleForNote(currentNote).getPanning());
			active = true;
			release = false;
			volumeEnvelope.reset();
			panningEnvelope.reset();
			fadeOutVol = 65536;
		}

		int newVolume = ud.getVolume();
		if (newVolume >= 0x10 && newVolume <= 0x50)
			setVolume(newVolume - 0x10);
	}

	private void updateVolume() {
		finalVol = (( currentVolume / 64f) * volumeScale);

		if (volumeEnvelope.use())
			finalVol *= (volumeEnvelope.getValue() / 64);

		if (mod.getGlobalVolume() != 64) finalVol *= ((double) mod.getGlobalVolume() / 64);

		if (release) {
			if (!volumeEnvelope.use()) {
				active = false;
			} else {
				finalVol *= ((float) fadeOutVol / 65536);
				fadeOutVol -= currentInstrument.getFadeout();
				if (fadeOutVol <= 10) {
					active = false;
					return;
				}
			}
		}
		samplePlayer.setVolume((int) (finalVol * 255));
	}

	private void updatePanning() {
		finalPan = panning;
		if (panningEnvelope.use()) {
			float panEnv = panningEnvelope.getValue();

			finalPan += (panEnv - 32) * (128 - Math.abs(panning - 128)) / 32.0f;
		}
		samplePlayer.setPanning( (int) ((finalPan / 256.0f) * 255));
	}

	public final void tick() {
		if (!active || currentInstrument == null) return;
		updateVolume();
		updatePanning();

		period = getPeriod(currentNote);

		doVibrato();

		// TODO: this should be somewhere else...
		int notePitch = (int) (((freq+freqDelta) / (double) mod.getDeviceSampleRate()) * 1024);

		if (samplePlayer.getPitch() < 0)
			samplePlayer.setPitch(-notePitch);
		else
			samplePlayer.setPitch(notePitch);
	}

	private int vibPos = 0;
	private int vibSweepPos = 0;
	private void doVibrato() {
		int delta = 0;
		Vibrato vib = currentInstrument.getVibrato();

		switch (vib.getType() & 3) {
			case 0: delta = (int) (Math.sin(2* Math.PI * vibPos / 256.0f) * 64);
				break;
			case 1:
				delta = 64;
				if (vibPos > 127)
					delta = -64;
			case 2: delta = (128 - ((vibPos + 128) % 256)) >> 1;
				break;
			case 3: delta = (128 - ((256 - vibPos)+128)%256) >> 1;
				break;
		};

		delta *= vib.getDepth();
		if (vib.getSweep() != 0)
			delta = delta * vibSweepPos / vib.getSweep();
		delta >>=7;
		delta <<=2;

		// TODO: this should be somewhere else...
		freq = getFreq(currentNote);
		freq += delta;

		vibSweepPos++;
		if (vibSweepPos > vib.getSweep())
			vibSweepPos = vib.getSweep();

		vibPos += vib.getRate();

		if (vibPos > 255)
			vibPos -= 256;
	}

}
