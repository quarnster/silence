/* Sample.java - Stores information about a sample
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

/**
 * Stores sample data
 *
 * @author Fredrik Ehnbom
 */
public class Sample {
	public static int FORWARD_LOOP = 0x1;
	public static int PINGPONG_LOOP = 0x2;

	private int quality = 0;

	private int loopType = 0;
	private int loopStart = 0;
	private int loopEnd = 0;

	private byte relativeNote = 0;
	private byte fineTune = 0;
	private int volume;
	private int panning;

	private int length = 0;
	private short[] data;


	public int getVolume() { return volume; }
	public void setVolume(int vol) { volume = vol; }

	public int getPanning() { return panning; }
	public void setPanning(int p) { panning = p; }

	public void setQuality(int q) { quality = q; }
	public int getQuality() { return quality; }

	public void setLoopType(int loopType) { this.loopType = loopType; }
	public int getLoopType() { return loopType; }

	public void setLoopStart(int ls) { loopStart = ls; }
	public int getLoopStart() { return loopStart; }

	public void setLoopEnd(int le) { loopEnd = le; }
	public int getLoopEnd() { return loopEnd; }

	public void setFineTune(byte ft) { fineTune = ft; }
	public int getFineTune() { return fineTune; }

	public void setRelativeNote(byte rel) { relativeNote = rel; }
	public byte getRelativeNote() { return relativeNote; }

	public void setData(short[] data) { this.data = data; }
	public short[] getData() { return data; }

	public void setLength(int l) { length = l; }
	public int getLength() { return length; }
}
