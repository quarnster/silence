/* EnvelopeHandler.java - Handles envelopes
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

import java.awt.Point;
import silence.format.xm.data.Envelope;

/**
 * This class handles envelopes
 *
 * @author Fredrik Ehnbom
 */
class EnvelopeHandler {

	private Envelope	envelope;
	private boolean		enabled		= false;
	private boolean		release		= false;
	private boolean		sustain		= false;

	private float		kadd		= 0;
	private float		value		= 64;

	private int		length		= 0;
	private int		pos		= 0;

	public void release() {
		release = true;
	}

	public boolean use() {
		return envelope.isOn();
	}

	public void reset() {
		if (envelope == null) enabled = false;
		pos = 0;
		enabled = envelope.isOn();
		sustain = (pos == envelope.getSustainPosition() && (envelope.getType() & Envelope.SUSTAIN) != 0);
		update();
		if (!enabled) value = 64;
		release = false;
	}

	public void setEnvelope(Envelope e) {
		this.envelope = e;
		reset();
	}

	private void update() {
		Point[] data = envelope.getData();
		if (data == null || data.length == 0) return;
		value = data[pos].y;

		if (pos+1 != data.length) {
			length = (
					data[pos + 1].x -
					data[pos + 0].x
				);

			kadd = (float) (
				data[pos + 1].y -
				data[pos + 0].y
			) /
			(float) (
				length
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

				if ((pos == envelope.getSustainPosition() && (envelope.getType() & Envelope.SUSTAIN) != 0)) {
					sustain = true;
				} else if (pos == envelope.getLoopEnd()) {
					if ((envelope.getType() & Envelope.LOOP) != 0) {
						pos = envelope.getLoopStart();
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
