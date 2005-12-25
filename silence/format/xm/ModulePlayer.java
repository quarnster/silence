/* ModulePlayer.java - plays module files
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

import java.io.*;
import java.net.URL;

import silence.format.xm.data.*;


/**
 * The general module player
 *
 * @author Fredrik Ehnbom
 */
public class ModulePlayer
{
	private Module module;

	private int tempo = 0;
	private int bpm = 0;

	private int globalVolume = 64;
	private void calculateSamplesPerTick() { samplesPerTick = (5 * deviceSampleRate) / (2 * bpm); }

	public void setBpm(int b) { bpm = b; calculateSamplesPerTick(); }
	public int getBpm() { return bpm; }
	public void setTempo(int t) { tempo = t; tick = 0; }
	public int getTempo() { return tempo; }

	public int getGlobalVolume() { return globalVolume; }
	public void setGlobalVolume(int v) {
		globalVolume = v > 64 ? 64 : v < 0 ? 0 : v;
	}

	public int getOrder() { return order; }
//	public void setOrder(int o) { System.out.print("TODO: setOrder"); }
	public int getRow() { return row; }
//	public void setRow() { System.


	public void setModule(Module mod) {
		module = mod;
		setTempo(module.getTempo());
		setBpm(module.getBpm());
		channel = new Channel[module.getChannelCount()];
		for (int i = 0; i < channel.length; i++) {
			channel[i] = new Channel(this);
		}
		order = 0;
		playingPattern = module.getPatterns()[module.getPatternOrder()[order]];;
	}
	public Module getModule() {
		return module;
	}

	public void setPatternDelay(int pd) { patternDelay = pd; }

	public int getTick() { return tick; }
	public void setTick(int tick) { this.tick = tick; }

	private int deviceSampleRate = 0;

	private int row = 0;
	private int order = 0;

	private int patternDelay = 0;

	private int tick = 0;

	private Pattern playingPattern = null;
	private int patternPos = 0;

	private int samplesPerTick;
	private int restTick = 0;

	private	Channel[]	channel;

	public void setDeviceSampleRate(int rate) {
		deviceSampleRate = rate;
		calculateSamplesPerTick();
	}
	public int getDeviceSampleRate() {
		return deviceSampleRate;
	}

	public ModulePlayer() {
	}

	private final static short clamp(int src) {
		src = src < -32768 ? -32768 : src > 32767 ? 32767 : src;
		return (short)src;
	}

	private boolean patternJump = false;
	private int	patternJumpOrder = -1;
	private int	patternJumpRow = -1;

	/**
	 * Jumps to the specific row in a specific pattern
	 * @param pattern Pattern to jump to
	 * @param endRow Row in pattern to jump to
	 */
	public void patternJump(int pattern, int endRow) {
		patternJump = true;
		patternJumpOrder = pattern;
		patternJumpRow = endRow;
	}

	public void setPatternJumpOrder(int order) {
		patternJumpOrder = order;
		patternJump = true;
	}
	public void setPatternJumpRow(int row) {
		patternJump = true;
		patternJumpRow = row;
	}

	private void executePatternJump() {
		int[] patOrder = module.getPatternOrder();
		if (patternJumpOrder == -1)
			patternJumpOrder = order+1;
		if (patternJumpRow == -1)
			patternJumpRow = 0;

		order = patternJumpOrder;
		if (order >= patOrder.length)
			order = module.getRestartPosition();

		playingPattern = module.getPatterns()[patOrder[order]];

		patternPos = 0;
		row = patternJumpRow;

		for (int rows = 0; rows < patternJumpRow; rows++) {
			for (int chan = 0; chan < channel.length; chan++) {
				patternPos = channel[chan].skip(playingPattern, patternPos);
			}
		}
		patternJump = false;
		patternJumpRow = -1;
		patternJumpOrder = -1;
	}

	private void tick(int position, int length) {
		if (tick >= tempo) {
			if (patternDelay > 0) {
				patternDelay--;
				tick = 0;
			} else {
				if (row == playingPattern.getRows() || patternJump) {
					executePatternJump();
				}
				for (int j = 0; j < channel.length; j++)  {
					patternPos = channel[j].update(playingPattern, patternPos);
				}
				row++;
				tick = 0;
			}
		}
		int read = samplesPerTick;
		if (read > length) {
			// Calculate how many samples there are still to play in
			// this tick
			read = length;
			restTick = samplesPerTick - read;
		}

		for (int j = 0; j < channel.length; j++)  {
			channel[j].updateTick();
			channel[j].play(left, right, position, read);
		}

		tick++;
	}

	int[] left;
	int[] right;

	/**
	 * Play...
	 */
	public int read(int[] buffer, int off, int len) {
		if (left == null || off+len > left.length) {
			left = new int[off+len];
			right = new int[off+len];
		}
		for(int i = off; i < off+len; i++) {
			left[i] = 0;
		}
		System.arraycopy(left, off, right,off, len );
		int realLen = len;
		int tmpLen = len;
		int tmpOff = off;

		for (int i = off; i < off+len; i++) {
			buffer[i] = 0;
		}

		if (restTick > 0) {
			int read = restTick > len ? len : restTick;
			for (int j = 0; j < channel.length; j++)  {
				channel[j].play(left, right, off, read);
			}

			off += read;
			len -= read;
			restTick -= read;
		}

		while (len > 0) {
			int read = samplesPerTick < len ? samplesPerTick : len;
			tick(off, len);
			off += read;
			len -= read;
		}
		for (int i = tmpOff; i < tmpOff+tmpLen; i++) {
			int l = clamp(left[i])&0xffff;
			int r = clamp(right[i]) &0xffff;
			buffer[i] = r << 16 | l;
		}
		return realLen;
	}
}
