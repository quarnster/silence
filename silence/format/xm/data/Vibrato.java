/* Vibrato.java - handles vibrato data
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
 * Stores vibrato data
 *
 * @author Fredrik Ehnbom
 */
public class Vibrato {
	private int		type;
	private int		sweep;
	private int		depth;
	private int		rate;

	public int getType() { return type; }
	public void setType(int t) { type = t; }

	public void setSweep(int s) { sweep = s; }
	public int getSweep() { return sweep; }

	public void setDepth(int s) { depth = s; }
	public int getDepth() { return depth; }

	public void setRate(int s) { rate = s; }
	public int getRate() { return rate; }
}
