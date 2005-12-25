/* SamplePlayer.java - takes care of playing a sample
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
 * This class handles the playing of a sample.
 * Usually, this is the part that the hardware takes
 * care of. All it knows about is a sample, its looping,
 * volume, panning, position and pitch.
 *
 * Cubic interpolation is performed on the sample for
 * better sound quality.
 *
 * @author Fredrik Ehnbom
 */
class SamplePlayer {

	private int	volume		= 255;
	private int	panning		= 128;

	private int	pitch		= 0;
	private int	position	= 0;

	private int	panl		= 0;
	private int	panr		= 0;
	private int	vol		= 1024;

	private Sample	sample		= null;

	public SamplePlayer() {
		setVolume(255);
		setPanning(128);
	}

	public void setSample(Sample s) { this.sample = s; }
	public Sample getSample() { return sample; }

	public void setVolume(int vol) {
		if (vol < 0) vol = 0;
		else if (vol > 255) vol = 255;
		volume = vol;
		this.vol = (int) ((vol / 255.0f) * 1024);
	}
	public int getVolume() {
		return volume;
	}
	public void setPanning(int pan) {
		if (pan < 0) pan = 0;
		else if (pan > 255) pan = 255;
		this.panning = pan;
		panl = (int) ((Math.sqrt((255 - panning) / 255.0f)) * 1024);
		panr = (int) ((Math.sqrt((panning) / 255.0f)) * 1024);

	}
	public int getPanning() {
		return panning;
	}

	public void setPitch(int p) { pitch = p; }
	public int getPitch() { return pitch; }

	public void setPosition(int position) {
		// TODO: loop and such???
		this.position = position << 10;
	}

	private void pingpong() {
		Sample s = getSample();
		int loopStart = s.getLoopStart();
		int loopEnd = s.getLoopEnd();
		while ((pitch < 0 && position < loopStart) || ((position >= loopStart + loopEnd) && pitch > 0)) {
			if (pitch < 0) {
				position = loopStart - position;
				position += loopStart;
			} else {
				position = (loopStart + loopEnd) - position;
				position = (loopStart + loopEnd-1) + position;
			}
			pitch = - pitch;
		}
	}

	public boolean play(int[] left, int[] right, int off, int len) {
		Sample s = getSample();
		if (s.getLength() == 0) return false;

		short[] sampleData = s.getData();
		for (int i = off; i < off+len; i++) {
			int pos = position >> 10;

			int sample = 0;

			float finpos = ((position / 1024.0f) - pos);
			pos++; // for the cubic spline interpolation

			short xm1 = sampleData[pos - 1];
			short x0  = sampleData[pos + 0];
			short x1  = sampleData[pos + 1];
			short x2  = sampleData[pos + 2];
			float a = (3 * (x0-x1) - xm1 + x2) / 2.0f;
			float b = 2*x1 + xm1 - (5*x0 + x2) / 2.0f;
			float c = (x1 - xm1) / 2.0f;
			sample = (int) (((((a * finpos) + b) * finpos + c) * finpos + x0));

			sample = (sample * vol) >> 10;

			left[i] += (sample * panl) >> 10;
			right[i] += (sample * panr) >> 10;

			position += pitch;

			if (pitch < 0 && position < s.getLoopStart()) {
				pingpong();
			} else if (position >= (s.getLoopStart() + s.getLoopEnd()) || position >> 10 >= s.getLength()) {
				int loopType = s.getLoopType();
				if ((loopType & Sample.PINGPONG_LOOP) != 0) {
					// pingpong loop
					pingpong();
				} else if ((loopType & Sample.FORWARD_LOOP) != 0) {
					// forward loop
					position -= s.getLoopStart() + s.getLoopEnd();
					position %= s.getLoopEnd();

					position += s.getLoopStart();
				} else {
					// no loop
					return false;
				}
			}
		}
		return true;
	}
}
