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

import silence.devices.*;

/**
 * The basic class for silence.
 * @author Fredrik Ehnbom
 * @version $Id: Silence.java,v 1.4 2000/06/25 18:42:09 quarn Exp $
 */
public class Silence {

	private Vector devices = new Vector();
	private Hashtable nativelibs = new Hashtable();

	public static final String BassDevice  = "silence.devices.bass.BassDevice";
	public static final String FmodDevice  = "silence.devices.fmod.FmodDevice";
	public static final String MidasDevice = "silence.devices.midas.MidasDevice";
	public static final String MuhmuDevice = "silence.devices.MuhmuDevice";

	public Silence() {
		addDevice(BassDevice,  "bsilence");
		addDevice(FmodDevice,  "fsilence");
		addDevice(MidasDevice, "msilence");
		addDevice(MuhmuDevice, "");
	}

	/**
	 * Add a device to the devicelist
	 * @param device The device to add
	 * @param library The name of the native library if one should be loaded.
	 * just use "" if the device has no native library.
	 */
	public void addDevice(String device, String library) {
		devices.addElement(device);
		nativelibs.put(device, library);
	}

	/**
	 * Load the specified device
	 * @param device The device to load
	 * @return The AudioDevice if it could be loaded or null if it could not
	 */
	public AudioDevice loadDevice(String device) throws AudioException {
		System.out.println(device);
		try {
			String library = nativelibs.get(device).toString();
			if (!library.equals("")) System.loadLibrary(library);

			Class c = Class.forName(device);
			return (AudioDevice) c.newInstance();
		} catch (Throwable t) {
			throw new AudioException("failed to load device \"" + device + "\": " + t.toString());
		}
	}

	/**
	 * Tries to load a device from the device list
	 * until one could be loaded
	 * @return The first AudioDevice that could be loaded
	 */
	public AudioDevice getAudioDevice() {
		AudioDevice audio = null;
		Enumeration e = devices.elements();

		while (e.hasMoreElements()) {
			try {
				audio = loadDevice(e.nextElement().toString());
				if (audio != null) return audio;
			} catch (AudioException ae) {
				System.err.println(ae);
			}
		}

		return audio;
	}
}
/*
 * ChangeLog:
 * $Log: Silence.java,v $
 * Revision 1.4  2000/06/25 18:42:09  quarn
 * loadDevice now throws an exception if the device could not be loaded
 *
 * Revision 1.3  2000/06/25 15:57:10  quarn
 * now does something usefull
 *
 * Revision 1.1.1.1  2000/04/29 10:21:19  quarn
 * initial import
 */
