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
 *
 * Midas Digital Audio System is Copyright (c) 1996-1999 Housemarque Inc.
 * You can download Midas from http://www.s2.org/midas/
 * @author Fredrik Ehnbom
 * @version $Id: MidasDevice.java,v 1.5 2000/05/27 10:11:30 quarn Exp $
 */
public class MidasDevice extends AudioDevice implements Runnable {

	static {
		System.loadLibrary("midasglue");
	}

	/**
	 * Check if a sync event has occured.
	 * @return -1 if it has not, or the syncnumber if it has
	 */
	private native int synced();

	/**
	 * Returns the name of this device
	 */
	public String getName() {
		return "Midas";
	}

	/**
	 * Returns the homepage url for this device
	 */
	public String getUrl() {
		return "http://www.s2.org/midas/";
	}

	/**
	 * Initialize the device
	 * @param sound If we should play the song regular or in nosound mode
	 */
	public native void init(boolean sound) throws MidasException;

	/**
	 * The native play function
	 * @param file The file to play
	 * @param loop Wheter to loop or not
	 */
	private native void Nplay(String file, boolean loop) throws MidasException;

	/**
	 * The native stop function
	 */
	private native void Nstop();

	/**
	 * Close and clean up
	 */
	public native void close();

	/**
	 * Set the volume
	 * @param volume The new volume
	 */
	public native void setVolume(int volume);

	/**
	 * Quick hack for the syncing to work
	 */
	private Thread t = null;

	/**
	 * Creates a new MidasDevice
	 */
	public MidasDevice() {
	}

	/**
	 * A quick hack for the syncing
	 */
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
	 * @param synceff The synceffect parameter
	 */
	public void sync(int synceff) {
		System.out.println("sync: " + synceff);
	}

	/**
	 * Starts playing the file
	 * @param file The file to play
	 * @param loop Wheter to loop or not
	 */
	public void play(String file, boolean loop) throws MidasException {
		Nplay(file, loop);

		if (t == null) {
			t = new Thread(this);
			t.start();
		}
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
	public void stop() {
		Nstop();

		t = null;
	}
}
/*
 * ChangeLog:
 * $Log: MidasDevice.java,v $
 * Revision 1.5  2000/05/27 10:11:30  quarn
 * added getName and getUrl functions
 *
 * Revision 1.4  2000/05/07 09:30:10  quarn
 * Added setVolume method, some fixes
 *
 * Revision 1.3  2000/04/30 13:19:03  quarn
 * choose which file to play in the play method instead of init
 *
 * Revision 1.2  2000/04/29 10:33:52  quarn
 * drats! Wrote  instead of ...
 *
 * Revision 1.1.1.1  2000/04/29 10:21:20  quarn
 * initial import
 *
 *
 */
