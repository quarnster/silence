/* Silence.java - The basic class for silence
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

import java.util.*;

import org.komplex.audio.*;

import silence.format.AudioFormat;

/**
 * The basic class for silence.
 * @author Fredrik Ehnbom
 * @version $Id: Silence.java,v 1.5 2000/08/25 17:36:26 quarn Exp $
 */
public class Silence implements AudioConstants {

	private AudioOutDevice	device = null;
	private AudioFormat	format = null;

	public Silence() {
	}

	/**
	 * Tries to load and init an audiodevice.
	 * @param soundFormat The format for the audio. You will most likely use one of the FORMAT_* variables.
	 * @param sound Wheter we want sound output or not (sound or nosound mode)
	 */
	public void init(int soundFormat, boolean sound) throws AudioException {
		AudioOutDeviceFactory factory = new AudioOutDeviceFactory();

		if (sound == false) {
			String[] args = {"-nosound"};
			factory.initArgs(args);
		}

		while (device == null) {
			try {
				device = factory.getAudioOutDevice();

				if (device == null) {
					break;
				}

				// direcsound requires a component reference
				Hashtable hash = new Hashtable();
				hash.put(AudioConstants.PROP_COMPONENT, this);
				device.setProperties(hash);

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
	 * Loads the AudioFormat for the specified file.
	 * @param file The file to load
	 */
	public AudioFormat load(String file) throws AudioException {
		if (file.indexOf(".") == -1) throw new AudioException("Does not know how to play " + file);

		String end = file.substring(file.lastIndexOf("."), file.length());

		// get the AudioFormat for this file
		AudioFormat format = AudioFormat.getFormat(end);

		if (format == null) throw new AudioException("Does not know how to play " + file);

		try {
			format.load(file);
		} catch (Exception e) {
			throw new AudioException(e.toString());
		}

		return format;
	}

	/**
	 * Plays (or loops) the specified AudioFormat.
	 * @param format The AudioFormat to play
	 * @param loop Wheter we want to loop or not
	 */
	public void play(AudioFormat format, boolean loop) throws AudioException {
		if (device == null) throw new AudioException("You must create a device first!");

		this.format = format;
		format.setSampleRate(device.getSampleRate());
		device.setPullSource(format);

		try {
			device.start();
		} catch (org.komplex.audio.AudioException e) {
			throw new AudioException(e.toString());
		}
	}

	/**
	 * Stops playing
	 */
	public void stop() {
		device.stop();
	}

	/**
	 * Set the volume for the audio output
	 * @param volume The new volume ranging from 0-100
	 */
	public void setVolume(int volume) {
		volume = (volume > 100) ? 100 : (volume < 0) ? 0 : volume;

		format.setVolume((double) volume / 100);
	}
}
/*
 * ChangeLog:
 * $Log: Silence.java,v $
 * Revision 1.5  2000/08/25 17:36:26  quarn
 * is now like the new version of devices/MuhmuDevice.java
 *
 * Revision 1.4  2000/06/25 18:42:09  quarn
 * loadDevice now throws an exception if the device could not be loaded
 *
 * Revision 1.3  2000/06/25 15:57:10  quarn
 * now does something usefull
 *
 * Revision 1.1.1.1  2000/04/29 10:21:19  quarn
 * initial import
 */
