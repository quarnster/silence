/* FmodException.java - FmodException
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

package silence.devices.fmod;

import silence.AudioException;

/**
 * FmodException
 * @author Fredrik Ehnbom
 * @version $Id: FmodException.java,v 1.1 2000/05/07 14:13:47 quarn Exp $
 */
public class FmodException extends AudioException {

	public FmodException(String exception) {
		super(exception);
	}
}
/*
 * ChangeLog:
 * $Log: FmodException.java,v $
 * Revision 1.1  2000/05/07 14:13:47  quarn
 * the FMOD device
 *
 */
