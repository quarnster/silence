/* CallbackClass.java - The interface for callbacks
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

package silence;

/**
 * The interface for callbacks.
 * Implement this class if you need
 * to sync your production to the music
 * @author Fredrik Ehnbom
 * @version $Id: CallbackClass.java,v 1.1 2000/06/25 18:37:42 quarn Exp $
 */
public interface CallbackClass {

	/**
	 * Called when the device finds a
	 * synceffect
	 * @param param The parameter to the synceffect
	 */
	public void syncCallback(int param);
}
/*
 * ChangeLog
 * $Log: CallbackClass.java,v $
 * Revision 1.1  2000/06/25 18:37:42  quarn
 * The callback interface
 *
 */