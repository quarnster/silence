/* MulawDecoder.java - decodes mulaw encoded data
 * Copyright (C) 2001-2005 Fredrik Ehnbom
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
package silence.format.au;

/**
 * Decodes mulaw encoded data
 *
 * @author Fredrik Ehnbom
 */
class MulawDecoder
	extends Decoder
{

	final int[] decode(byte[] source) {
		int[] conv = new int[source.length];

		for (int i = 0; i < source.length; i++) {
			int ulaw = (int) source[i];
			ulaw = ~ulaw;
	        	int exponent = (ulaw >> 4) & 0x7;
		        int mantissa = (ulaw & 0xf) + 16;
			int adjusted = (mantissa << (exponent + 3)) - 128 - 4;


	        	conv[i] = ((ulaw & 0x80) > 0) ? adjusted : -adjusted;
		}
		return conv;
	}
}
