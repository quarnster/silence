/* MuhmuDevice.java - An audiodevice which uses MuhmuAudio
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

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;

import org.komplex.audio.AudioConstants;
import org.komplex.audio.AudioOutDevice;
import org.komplex.audio.AudioOutDeviceFactory;
import org.komplex.audio.PullAudioSource;

import silence.AudioException;
import silence.format.xm.Xm;

/**
 * An audiodevice which uses MuhmuAudio by Jarno Heikkinen
 * &lt;<a href="mailto:jarnoh@komplex.org">jarnoh@komplex.org</a>&gt;.<br>
 * For more information about MuhmuAudio please visit
 * <a href="http://muhmuaudio.sourceforge.net">http://muhmuaudio.sourceforge.net</a>
 * @author Fredrik Ehnbom
 * @version $Id: MuhmuDevice.java,v 1.3 2000/06/25 18:39:08 quarn Exp $
 */
public class MuhmuDevice extends AudioDevice {

	private AudioOutDevice  device = null;
	private PullAudioSource source = null;

	/**
	 * Create a new MuhmuDevice
	 */
	public MuhmuDevice() {
	}

	/**
	 * Returns the name of this device
	 */
	public String getName() {
		return "MuhmuAudio";
	}

	/**
	 * Returns the home url for this device
	 */
	public String getUrl() {
		 return "http://muhmuaudio.sourceforge.net";
	}

	/**
	 * Init the AudioDevice
	 * @param sound If we should play the sound or if we are in nosound mode
	 */
	public void init(boolean sound) throws AudioException {
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

				// directsound requires a component reference
				Hashtable hash = new Hashtable();
				hash.put(AudioConstants.PROP_COMPONENT, this);
				device.setProperties(hash);

				device.init(device.FORMAT_PCM44K16S);

				break;
			} catch(Throwable e) {
				factory.disableDevice(device);
				device = null;
			}
		}

		if (device == null) {
			throw new AudioException("Could not find a device to use...");
		}
	}

	/**
	 * Start playing the file
	 * @param file The file to play
	 * @param loop Wheter to loop or not
	 */
	public void play(String file, boolean loop) throws AudioException {
		if (file.toLowerCase().endsWith(".xm")) {
			try {
				if (file.indexOf(":") != -1) {
					// the song should load from an URL
					URL u = new URL(file);
					source = new Xm(u.openStream());
				} else {
					// it is a file
					source = new Xm(new FileInputStream(file));
				}
			} catch (Throwable t) {
				throw new AudioException(t.toString());
			}
		} else {
			throw new AudioException(file + " can not be played by this device");
		}

		device.setPullSource(source);
		try {
			device.start();
		} catch (org.komplex.audio.AudioException e) {
			throw new AudioException(e.toString());
		}
	}

	/**
	 * Stop playing the file
	 */
	public void stop() {
		device.stop();
	}

	/**
	 * Pause the playing of the file
	 */
	public void pause() {}

	/**
	 * Sets the volume
	 * @param volume The new volume
	 */
	public void setVolume(int volume) {}

	/**
	 * Close and cleanup
	 */
	public void close() {}
}
/*
 * ChangeLog:
 * $Log: MuhmuDevice.java,v $
 * Revision 1.3  2000/06/25 18:39:08  quarn
 * removed the sync method
 *
 * Revision 1.2  2000/06/20 23:22:05  quarn
 * no need to throw an exception on pause
 *
 */
