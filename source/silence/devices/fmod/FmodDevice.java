/* FmodDevice.java - An audio device which uses FMOD
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

import silence.devices.AudioDevice;

/**
 * An audio device which uses FMOD.<br>
 * FMOD is Copyright (c) 1999-2000 FireLight Multimedia.<br>
 * You can download FMOD from <a href="http://www.fmod.org">http://www.fmod.org</a><br>
 * @author Fredrik Ehnbom
 * @version $Id: FmodDevice.java,v 1.3 2000/06/08 16:27:05 quarn Exp $
 */
public class FmodDevice extends AudioDevice implements  Runnable {

	/**
	 * Check if a sync event has occured.
	 * @return -1 if it has not, or the syncnumber if it has
	 */
	private native int synced();

	/**
	 * Returns the name of this device
	 */
	public String getName() {
		return "Fmod";
	}

	/**
	 * Returns the homepage url for this device
	 */
	public String getUrl() {
		return "http://www.fmod.org";
	}

	/**
	 * Initialize the device
	 */
	public native void init(boolean sound) throws FmodException;

	/**
	 * The native play function
	 */
	private native void Nplay(String file, boolean loop) throws FmodException;

	/**
	 * Sets the volume
	 * @param volume The new volume
	 */
	public native void setVolume(int volume);

	/**
	 * The native stop function
	 */
	private native void Nstop();

	/**
	 * Close and clean up
	 */
	public native void close();

	/**
	 * Quick hack for the syncing to work
	 */
	private Thread t = null;

	/**
	 * Creates a new FmodDevice
	 */
	public FmodDevice() {
	}

	public void run() {
		int i;
		while (t != null) {
			if ((i = synced()) != -1) {
				sync(i);
			}
			try {
				// just to not hog all the cpu power...
				t.sleep(10);
			} catch (InterruptedException ie) {}
		}
	}

	/**
	 * This function is called when a sync event has occured.
	 */
	public void sync(int synceff) {
		System.out.println("sync: " + synceff);
	}

	/**
	 * Starts playing the file
	 */
	public void play(String file, boolean loop) throws FmodException {
		Nplay(file, loop);

		if (t == null) {
			t = new Thread(this);
			t.start();
		}
	}

	/**
	 * Pause music
	 */
        public native void pause() throws FmodException;

	/**
	 * Stops playing the file
	 */
	public void stop(){
		Nstop();

		t = null;
	}
}
/*
 * ChangeLog:
 * $Log: FmodDevice.java,v $
 * Revision 1.3  2000/06/08 16:27:05  quarn
 * moved the loadLibrary function to AudioDevice.java
 *
 * Revision 1.2  2000/05/27 10:11:30  quarn
 * added getName and getUrl functions
 *
 * Revision 1.1  2000/05/07 14:13:47  quarn
 * the FMOD device
 *
 */
