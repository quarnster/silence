/* Decoder.java - The abstract decoder class
 * Copyright (C) 2001 Fredrik Ehnbom
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
package org.gjt.fredde.silence.format.au;

/**
 * The abstract decoder class
 *
 * @author Fredrik Ehnbom
 * @version $Id: Decoder.java,v 1.1 2001/01/06 10:41:47 fredde Exp $
 */
abstract class Decoder {

	abstract int[] decode(byte[] source);
}
/*
 * ChangeLog:
 * $Log: Decoder.java,v $
 * Revision 1.1  2001/01/06 10:41:47  fredde
 * au decoders
 *
 */