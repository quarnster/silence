/* AudioFormat.java - The basic AudioFormat class
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
package org.gjt.fredde.silence.format;

import java.io.*;
import java.net.URL;
import java.util.Hashtable;

import org.komplex.audio.*;

/**
 * The basic class for AudioFormats
 *
 * @author Fredrik Ehnbom
 * @version $Id: AudioFormat.java,v 1.1 2000/09/25 16:34:34 fredde Exp $
 */
public abstract class AudioFormat
	implements PullAudioSource
{

	/** List of formats */
	private static Hashtable flist = new Hashtable();

	/** the samplerate for the device used */
	public int deviceSampleRate = 0;

	/** the volume */
	protected double volume = 1;

	/** play loud? */
	protected boolean playLoud = true;

	static {
		// put all supported formats to the format list
		flist.put(".xm", "org.gjt.fredde.silence.format.xm.Xm");
		flist.put(".au", "org.gjt.fredde.silence.format.au.Au");
	}

	/**
	 * Load the file
	 */
	public void load(String file)
		throws Exception
	{
		if (file.indexOf(":") > 1) {
			URL u = new URL(file);
			load(new BufferedInputStream(u.openStream()));
		} else {
			load(new BufferedInputStream(new FileInputStream(file)));
		}
	}

	/**
	 * Load the file from a BufferedInputStream
	 */
	protected abstract void load(BufferedInputStream is) throws IOException;

	/**
	 * Gets the AudioFormat for the specified format
	 *
	 * @param format The format we wish to get the AudioFormat for
	 */
	public static AudioFormat getFormat(String format) {
		format = format.toLowerCase();

		Object cl = flist.get(format);

		if (cl != null) {
			try {
				Class c = Class.forName(cl.toString());
				return (AudioFormat) c.newInstance();
			} catch (Throwable t) {
				System.err.println("couldn't create " + cl);
				t.printStackTrace();
			}
		} 

		return null;
	}

	/**
	 * Sets the volume
	 *
	 * @param volume The new volume
	 */
	public void setVolume(double volume) {
		this.volume = volume;
	}

	/**
	 * Sets the device used for playing this AudioFormat
	 *
	 * @param device The device to use
	 */
	public void setDevice(AudioOutDevice device) {
		playLoud = !(device instanceof org.komplex.audio.device.NoSoundDevice);
		deviceSampleRate = device.getSampleRate();
	}
}
/*
 * ChangeLog:
 * $Log: AudioFormat.java,v $
 * Revision 1.1  2000/09/25 16:34:34  fredde
 * Initial revision
 *
 */
