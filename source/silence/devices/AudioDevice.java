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

import silence.devices.fmod.*;
import silence.devices.midas.*;

/**
 * The basic class for audio devices
 * @author Fredrik Ehnbom
 * @version $Id: AudioDevice.java,v 1.9 2000/06/20 22:38:59 quarn Exp $
 */
public abstract class AudioDevice {

	/** Wheter or not we can use native code */
	private static boolean useNative = true;

	static {
		try {
			System.loadLibrary("silence");
		} catch (Throwable t) {
			useNative = false;
		}
	}

	/**
	 * Returns the AudioDevice for this platform
	 */
	public static AudioDevice getSystemAudioDevice() {
		String sys = System.getProperty("os.name");
		if (useNative) {
			if (sys.indexOf("Windows") != -1) {
				return new FmodDevice();
			} else if (sys.equals("Linux")) {
				return new MidasDevice();
			}
		}

		return new MuhmuDevice();
	}

	/**
	 * Returns the classname for the AudioDevice of this platform
	 */
	public static String getSystemAudioDeviceName() {
		String sys = System.getProperty("os.name");
		if (sys.indexOf("Windows") != -1) {
			return "silence.devices.fmod.FmodDevice";
		} else if (sys.equals("Linux")) {
			return "silence.devices.midas.MidasDevice";
		}

		return "silence.devices.MuhmuDevice";
	}

	/**
	 * Returns the name of this device
	 */
	public abstract String getName();

	/**
	 * Returns the home url for this device
	 */
	public abstract String getUrl();

	/**
	 * Init the AudioDevice
	 * @param sound If we should play the sound or if we are in nosound mode
	 */
	public abstract void init(boolean sound) throws AudioException;

	/**
	 * Start playing the file
	 * @param file The file to play
	 * @param loop Wheter to loop or not
	 */
	public abstract void play(String file, boolean loop) throws AudioException;

	/**
	 * Stop playing the file
	 */
	public abstract void stop();

	/**
	 * Pause the playing of the file
	 */
	public abstract void pause() throws AudioException;

	/**
	 * This function is called when a sync event occurs
	 */
	public abstract void sync(int effect);

	/**
	 * Sets the volume
	 * @param volume The new volume
	 */
	public abstract void setVolume(int volume);

	/**
	 * Close and cleanup
	 */
	public abstract void close();
}
/*
 * ChangeLog:
 * $Log: AudioDevice.java,v $
 * Revision 1.9  2000/06/20 22:38:59  quarn
 * now loads the silence library
 *
 * Revision 1.8  2000/06/08 16:22:33  quarn
 * added the 100% pure java MuhmuDevice
 *
 * Revision 1.7  2000/06/07 13:18:07  quarn
 * added getSystemAudioDeviceName function
 *
 * Revision 1.6  2000/05/27 10:11:30  quarn
 * added getName and getUrl functions
 *
 * Revision 1.5  2000/05/07 14:10:54  quarn
 * added getSystemAudioDevice function
 *
 * Revision 1.4  2000/05/07 09:27:07  quarn
 * Added setVolume method, added javadoc tags\n is now an abstract class instead of an interface
 *
 * Revision 1.3  2000/04/30 13:17:51  quarn
 * choose which file to play in the play method instead of init
 *
 * Revision 1.2  2000/04/29 10:33:52  quarn
 * drats! Wrote  instead of ...
 *
 * Revision 1.1.1.1  2000/04/29 10:21:19  quarn
 * initial import
 *
 *
 */
