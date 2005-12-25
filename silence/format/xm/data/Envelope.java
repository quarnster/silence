/* Envelope.java - Represents an envelope
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

import java.awt.Point;
/**
 * This class represents envelopes
 *
 * @author Fredrik Ehnbom
 */
public class Envelope {
	public static int	ON		= 0x1;
	public static int	SUSTAIN		= 0x2;
	public static int	LOOP		= 0x4;

	private Point[]		data;
	private int		loopStart	= 0;
	private int		loopEnd		= 0;
	private int		type		= 0;
	private int		sustainPosition	= 0;

	public boolean isOn() {
		return (type & ON) != 0;
	}

	public void setType(int type) { this.type = type; }
	public int getType() { return type; }

	public void setData(Point[] data) { this.data = data; }
	public Point[] getData() { return data; }

	public void setLoopStart(int ls) { loopStart = ls; }
	public int getLoopStart() { return loopStart; }

	public void setLoopEnd(int ll) { loopEnd = ll; }
	public int getLoopEnd() { return (type & LOOP) != 0 ? loopEnd : data.length; }

	public void setSustainPosition(int sp) { sustainPosition = sp; }
	public int getSustainPosition() { return sustainPosition; }
}
