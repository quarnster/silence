/* Silence.java - The basic class for silence
 * Copyright (C) 2000-2001 Fredrik Ehnbom
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

package org.gjt.fredde.silence;

import java.awt.Component;
import java.util.*;

import org.komplex.audio.*;

import org.gjt.fredde.silence.format.AudioFormat;

/**
 * The basic class for silence.
 *
 * @author Fredrik Ehnbom
 * @version $Id: Silence.java,v 1.7 2003/09/01 09:02:39 fredde Exp $
 */
public class Silence
	implements AudioConstants
{

	private AudioOutDevice device = null;

	public Silence() {
	}

	/**
	 * Sets the device to use for audioplaying
	 *
	 * @param device The AudioOutDevice
	 */
	public void setDevice(AudioOutDevice device) {
		this.device = device;
	}

	/**
	 * Gets the device used for audioplaying
	 */
	public AudioOutDevice getDevice() {
		return device;
	}


	/**
	 * Tries to load and init an audiodevice.
	 *
	 * @exception AudioException If a device could not be found
	 */
	public void init()
		throws AudioException
	{
		init(FORMAT_PCM44K16S, null);
	}

	/**
	 * Tries to load and init an audiodevice.
	 *
	 * @param soundFormat The format for the audio. You will most likely use one of the FORMAT_* variables.
	 * @exception AudioException If a device could not be found
	 */
	public void init(int soundFormat)
		throws AudioException
	{
		init(soundFormat, null);
	}

	/**
	 * Tries to load and init an audiodevice.
	 *
	 * @param soundFormat The format for the audio. You will most likely use one of the FORMAT_* variables.
	 * @param comp Directsound requires a component reference
	 * @exception AudioException If a device could not be found
	 */
	public void init(int soundFormat, Component comp)
		throws AudioException
	{
		AudioOutDeviceFactory factory = new AudioOutDeviceFactory();

		while (device == null) {
			try {
				device = factory.getAudioOutDevice();

				if (device == null) {
					break;
				}

				// directsound requires a component reference
				if (comp != null) {
					Hashtable hash = new Hashtable();
					hash.put(AudioConstants.PROP_COMPONENT, comp);
					device.setProperties(hash);
				}

				device.init(soundFormat);

				break;
			} catch (Throwable t) {
				factory.disableDevice(device);
				device = null;
			}
		}

		if (device == null) {
			throw new AudioException("Could not find a device to use...");
		}
	}

	/**
	 * Loads an AudioFormat for the specified file.
	 * This can also be an URL in textformat:<br><br>
	 * <code>load("http://httpsomewhere/afile")</code>,<br>
	 * <code>load("ftp://ftpsomewhere/afile")</code>,<br>
	 * etc...<br><br>
	 *
	 * All URLs supported by the java implementation
	 * is also supported by silence.
	 *
	 * @param file The file to load
	 * @exception AudioException If a file of unknown type is specified
	 */
	public AudioFormat load(String file)
		throws AudioException
	{
		if (file.indexOf(".") == -1) throw new AudioException("Does not know how to play " + file);

		String end = file.substring(file.lastIndexOf("."), file.length());

		/// -> ADDED 27.4.2002 ///
		if (end.indexOf(".gz") != -1) {///+
			end = file.substring(0, file.lastIndexOf("."));///+
			end = end.substring(end.lastIndexOf("."), end.length());///+
		}///+
		/// <- ///

		// get the AudioFormat for this file
		AudioFormat format = AudioFormat.getFormat(end);

		if (format == null) throw new AudioException("Does not know how to play " + file);

		try {
			format.load(file);
		} catch (Exception e) {
			throw new AudioException(e);
		}

		return format;
	}

	/**
	 * Plays the specified AudioFormat.
	 *
	 * @param format The AudioFormat to play
	 */
	public void play(AudioFormat format)
		throws AudioException
	{
		play(format, false);
	}

	/**
	 * Plays (or loops) the specified AudioFormat.
	 *
	 * @param format The AudioFormat to play
	 * @param loop Wheter we want to loop or not
	 */
	public void play(AudioFormat format, boolean loop)
		throws AudioException
	{
		if (device == null) throw new AudioException("You must create a device first!");

		format.setDevice(device);
		device.setPullSource(format);

		try {
			device.start();
		} catch (org.komplex.audio.AudioException e) {
			throw new AudioException(e);
		}
	}

	/**
	 * Stops playing
	 */
	public void stop() {
		device.stop();
	}
}
/*
 * ChangeLog:
 * $Log: Silence.java,v $
 * Revision 1.7  2003/09/01 09:02:39  fredde
 * AudioException update
 *
 * Revision 1.6  2002/04/27 14:49:40  fredde
 * added patch for .gz-files by Henrik Raula
 *
 * Revision 1.5  2001/01/27 18:17:02  fredde
 * removed that ridiculous nosound mode
 *
 * Revision 1.4  2001/01/11 20:23:23  fredde
 * Added getDevice method
 *
 * Revision 1.3  2001/01/06 10:39:48  fredde
 * added the setDevice method
 *
 * Revision 1.2  2000/09/29 19:36:56  fredde
 * removed unused stuff, added javadoc comments
 * and some new functions
 *
 * Revision 1.1.1.1  2000/09/25 16:34:34  fredde
 * initial commit
 *
 */
