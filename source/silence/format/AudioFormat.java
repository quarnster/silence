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
package silence.format;

import java.io.*;
import java.net.URL;
import java.util.Hashtable;

import org.komplex .audio.PullAudioSource;

/**
 * The basic class for AudioFormats
 * @author Fredrik Ehnbom
 * @version $Id: AudioFormat.java,v 1.4 2000/09/03 17:40:18 quarn Exp $
 */
public abstract class AudioFormat implements PullAudioSource {

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
		flist.put(".xm", "silence.format.xm.Xm");
		flist.put(".au", "silence.format.au.Au");
	}

	/**
	 * Load the file
	 */
	public void load(String file) throws Exception {
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
	 * Set the samplerate for the device used
	 * @param rate The samplerate
	 */
	public void setSampleRate(int rate) {
		deviceSampleRate = rate;
		System.out.println("rate: " + rate);
	}

	/**
	 * Wheter we will acctually play sounds or not
	 */
	public void setPlayLoud(boolean loud) {
		playLoud = loud;
	}

	/**
	 * Sets the volume
	 * @param volume The new volume
	 */
	public void setVolume(double volume) {
		this.volume = volume;
	}
}
/*
 * ChangeLog:
 * $Log: AudioFormat.java,v $
 * Revision 1.4  2000/09/03 17:40:18  quarn
 * added playLoud stuff
 *
 * Revision 1.3  2000/08/25 17:37:33  quarn
 * added volume stuff
 *
 * Revision 1.2  2000/08/20 17:57:01  quarn
 * added .au format, samplerate stuff
 *
 * Revision 1.1  2000/07/21 09:37:34  quarn
 * the basic AudioFormat class
 *
 */
