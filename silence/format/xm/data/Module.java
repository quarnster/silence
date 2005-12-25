/* Module.java - Handles a module
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

import java.io.*;
import java.net.URL;

/**
 * Handles a modules data
 *
 * @author Fredrik Ehnbom
 */
public class Module
{
	private String title = "";
	private int patternOrder[];
	private int restartPosition;

	private boolean amigaFreqTable = false;

	private int tempo = 0;
	private int bpm = 0;

	private	Pattern[]	pattern;
	private Instrument[]	instrument;

	private int channelCount;

	public void setAmigaFreqTable(boolean b) { amigaFreqTable = b; }
	public boolean getAmigaFreqTable() { return amigaFreqTable; }

	public void setTitle(String title) { this.title = title; }
	public String getTitle() { return title; }

	public void setChannelCount(int c) { channelCount = c; }
	public int getChannelCount() { return channelCount; }

	public void setPatternOrder(int[] order) { patternOrder = order; }
	public int[] getPatternOrder() { return patternOrder; }

	public void setRestartPosition(int r) { restartPosition = r; }
	public int getRestartPosition() { return restartPosition; }

	public void setPatterns(Pattern[] pat) { pattern = pat; }
	public Pattern[] getPatterns() { return pattern; }

	public void setInstruments(Instrument[] i) { instrument = i; }
	public Instrument[] getInstruments() { return instrument; }

	public Instrument getInstrument(int idx) {
		if (idx < instrument.length)
			return instrument[idx];
		return null;
	}

	public void setBpm(int b) { bpm = b;}
	public int getBpm() { return bpm; }

	public void setTempo(int t) { tempo = t; }
	public int getTempo() { return tempo; }
}
