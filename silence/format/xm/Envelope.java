/* $Id: Envelope.java,v 1.1 2003/08/23 13:44:59 fredde Exp $
 * Copyright (C) 2000-2003 Fredrik Ehnbom
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
package org.gjt.fredde.silence.format.xm;

import java.awt.Point;
/**
 * This class handles envelopes
 *
 * @author Fredrik Ehnbom
 * @version $Revision: 1.1 $
 */
public class Envelope {

	private Point[]		data;
	private boolean		enabled		= false;
	private boolean		release		= false;
	private boolean		sustain		= false;

	private float		kadd		= 0;
	private float		value		= 64;
	private int		loopStart	= 0;
	private int		loopLen		= 0;
	private int		length		= 0;
	private int		type		= 0;
	private int		pos		= 0;
	private int		sustainPos	= 0;

	public void release() {
		release = true;
	}

	public boolean use() {
		return (type & 0x1) != 0;
	}

	public void reset() {
		pos = 0;
		enabled = (type & 0x1) != 0;
		sustain = (pos == sustainPos && (type & 0x2) != 0);
		update();
		if (!enabled) value = 64;
		release = false;
	}

	public void setData(Instrument currentInstrument) {
		data		= currentInstrument.volumeEnvelopePoints;
		sustainPos	= currentInstrument.volSustain;
		type		= currentInstrument.volType;

		if ((type & 0x4) != 0)
			loopLen = currentInstrument.volLoopEnd;
		else
			loopLen = currentInstrument.volumeEnvelopePoints.length - 1;

		loopStart = currentInstrument.volLoopStart;
		reset();
	}

	private void update() {
		if (data.length == 0) return;
		value = data[pos].y;

		if (pos+1 != data.length) {
			kadd = (float) (
				data[pos + 0].y -
				data[pos + 1].y
			) /
			(float) (
				data[pos + 0].x -
				data[pos + 1].x
			);

			length = (
					data[pos + 1].x -
					data[pos + 0].x
				);
		} else {
			enabled = false;
		}
	}

	public final float getValue() {
		if (enabled && (release || !sustain)) {
			value += kadd;
			if (length <= 0) {
				pos++;

				if ((pos == sustainPos && (type & 0x2) != 0)) {
					sustain = true;
				} else if (pos == loopLen) {
					if ((type & 0x4) != 0) {
						pos = loopStart;
					} else {
						enabled = false;
					}
				}
				update();
			}
			length--;
		}
		return value;
	}

}
/*
 * ChangeLog:
 * $Log: Envelope.java,v $
 * Revision 1.1  2003/08/23 13:44:59  fredde
 * moved envelope stuff from InstrumentManager to Envelope
 *
 */
