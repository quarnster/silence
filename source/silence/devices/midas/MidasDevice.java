/* MidasDevice.java - An audio device which uses midas
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

package silence.devices.midas;

import silence.devices.AudioDevice;

/**
 * An audio device which uses midas
 * @author Fredrik Ehnbom
 * @version $Id: MidasDevice.java,v 1.2 2000/04/29 10:33:52 quarn Exp $
 */
public class MidasDevice implements AudioDevice, Runnable {

	static {
		System.loadLibrary("midasglue");
	}

	/**
	 * Check if a sync event has occured.
	 * @return -1 if it has not, or the syncnumber if it has
	 */
	private native int synced();

	/**
	 * Initialize the device
	 */
	public native void init(String file, boolean nosound) throws MidasException;

	/**
	 * The native play function
	 */
	public native void Nplay() throws MidasException;

	/**
	 * The native stop function
	 */
	public native void Nstop();

	/**
	 * Close and clean up
	 */
	public native void close();

	/**
	 * Quick hack for the syncing to work
	 */
	private Thread t = null;

	/**
	 * Creates a new MidasDevice
	 */
	public MidasDevice() {
	}

	public void run() {
		int i;
		while (t != null) {
			if ((i = synced()) != -1) {
				sync(i);
			}
			try {
				// just to not hog all the cpu power...
				t.sleep(1);
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
	public void play() throws MidasException {
		Nplay();

		if (t == null) {
			t = new Thread(this);
		}
		t.start();
	}

	/**
	 * Pause music
	 */
	public void pause() throws MidasException {
		throw new MidasException("Pause is not supported yet...");
	}

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
 * $Log: MidasDevice.java,v $
 * Revision 1.2  2000/04/29 10:33:52  quarn
 * drats! Wrote  instead of ...
 *
 * Revision 1.1.1.1  2000/04/29 10:21:20  quarn
 * initial import
 *
 *
 */
