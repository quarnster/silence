/* AudioDevice.java - The basic class for audio devices
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

package silence.devices;

import silence.AudioException;

/**
 * The basic class for audio devices
 * @author Fredrik Ehnbom
 * @version $Ld$
 */
public interface AudioDevice {

	/**
	 * Init the AudioDevice
	 */
	public void init(String file, boolean nosound) throws AudioException;

	/**
	 * Start playing the file
	 */
	public void play() throws AudioException;

	/**
	 * Stop playing the file
	 */
	public void stop();

	/**
	 * Pause the playing of the file
	 */
	public void pause() throws AudioException;

	/**
	 * This function is called when a sync event occurs
	 */
	public void sync(int effect);

	/**
	 * Close and cleanup
	 */
	public void close();
}
/*
 * ChangeLog:
 * $Log: AudioDevice.java,v $
 * Revision 1.1  2000/04/29 10:21:19  quarn
 * Initial revision
 *
 */
