/* BassException.java - BassException
 * Copyright (C) 2000 Fredrik Ehnbom
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

package silence.devices.bass;

import silence.AudioException;

/**
 * BassException
 * @author Fredrik Ehnbom
 * @version $Id: BassException.java,v 1.1 2000/06/10 18:10:04 quarn Exp $
 */
public class BassException extends AudioException {

	public BassException(String exception) {
		super(exception);
	}
}
/*
 * ChangeLog:
 * $Log: BassException.java,v $
 * Revision 1.1  2000/06/10 18:10:04  quarn
 * the BassDevice
 *
 */
