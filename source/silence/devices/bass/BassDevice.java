/* BassDevice.java - An audio device which uses BASS
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

import silence.devices.AudioDevice;

/**
 * An audio device which uses BASS.<br>
 * BASS is Copyright (c) 1999-2000 Ian Luck. All rights reserved.<br>
 * You can download BASS from <a href="http://www.un4seen.com/music/">http://www.un4seen.com/music/</a><br>
 * @author Fredrik Ehnbom
 * @version $Id: BassDevice.java,v 1.1 2000/06/10 18:10:04 quarn Exp $
 */
public class BassDevice extends AudioDevice implements  Runnable {

	/**
	 * Check if a sync event has occured.
	 * @return -1 if it has not, or the syncnumber if it has
	 */
	private native int synced();

	/**
	 * Returns the name of this device
	 */
	public String getName() {
		return "BASS";
	}

	/**
	 * Returns the homepage url for this device
	 */
	public String getUrl() {
		return "http://www.un4seen.com/music/";
	}

	/**
	 * Initialize the device
	 */
	public native void init(boolean sound) throws BassException;

	/**
	 * The native play function
	 */
	private native void Nplay(String file, boolean loop) throws BassException;

	/**
	 * Sets the volume
	 * @param volume The new volume with a value between 0 and 100
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
	 * Creates a new BassDevice
	 */
	public BassDevice() {
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
	public void play(String file, boolean loop) throws BassException {
		Nplay(file, loop);

		if (t == null) {
			t = new Thread(this);
			t.start();
		}
	}

	/**
	 * Pause music
	 */
        public native void pause() throws BassException;

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
 * $Log: BassDevice.java,v $
 * Revision 1.1  2000/06/10 18:10:04  quarn
 * the BassDevice
 *
 */
