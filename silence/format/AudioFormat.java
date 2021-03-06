/* AudioFormat.java - The basic AudioFormat class
 * Copyright (C) 2000-2005 Fredrik Ehnbom
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

/// ADDED 27.4.2002 -> ///
import java.util.zip.*; ///+
/// <- ///

import org.komplex.audio.*;

/**
 * The basic class for AudioFormats. If you want to add support
 * for, lets say, .mp3-files this is the class to extend.
 *
 * @author Fredrik Ehnbom
 */
public abstract class AudioFormat
	implements PullAudioSource
{

	/** List of formats */
	private static Hashtable flist = new Hashtable();

	/** The AudioOutDevice used to play this format */
	protected AudioOutDevice device;

	static {
		// put all supported formats to the format list
		flist.put(".xm", "silence.format.xm.Xm");
		flist.put(".au", "silence.format.au.Au");
		flist.put(".ogg", "silence.format.ogg.Ogg");
		flist.put(".mp3", "silence.format.mp3.Mp3");
	}

	/**
	 * Adds a handler for the specified extension.
	 * Example: <br><br>
	 * <code>
	 * AudioFormat.addFormat(".mp3", "mypackage.myformats.MyMp3Format");
	 * </code>
	 * <p>
	 * Why is it like this and not <code>AudioFormat.addFormat(".mp3", new MyMp3Format())</code>?
	 * <br>
	 * Because if we do it like it is now the class does not have to
	 * be loaded if it is not used. Also you do not have to ship
	 * AudioFormats with your program that you know that you will not
	 * use. I got the idea when I saw something simular in
	 * <a href="http://www.sourceforge.net/projects/muhmuaudio">MuhmuAudio</a>
	 * so greetings to Jarno.
	 *
	 * @param extension The extension for the fileformat handled
	 * @param handler The handler for the extension
	 */
	public static void addFormat(String extension, String handler) {
		flist.put(extension, handler);
	}

	/**
	 * Load the file. It can also be an URL in textformat:<br><br>
	 * <code>load("http://httpsomewhere/afile")</code>,<br>
	 * <code>load("ftp://ftpsomewhere/afile")</code>,<br>
	 * etc...<br><br>
	 *
	 * All URLs supported by the java implementation
	 * is also supported by silence.
	 *
	 * @param file The file (or URL) to load
	 */
	public final void load(String file)
		throws Exception
	{
		if (file.indexOf(":") > 1) {
			URL u = new URL(file);

			/// ADDED 27.4.2002 -> ///
			if (file.indexOf(".gz") > 1) { ///+
				load(new BufferedInputStream(new GZIPInputStream(u.openStream()))); ///+
			} else { ///+
				load(new BufferedInputStream(u.openStream()));
			} ///+
			/// <- ///
		} else {
			if (file.indexOf(".gz") > 1) {
				load(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
			} else {
				load(new BufferedInputStream(new FileInputStream(file)));
			}
		}
	}

	/**
	 * Load the file from a BufferedInputStream
	 */
	public abstract void load(BufferedInputStream is) throws IOException;

	/**
	 * Cleanup resources used, ie the BufferedInputStream used for
	 * streaming
	 */
	public abstract void close();

	/**
	 * Gets the AudioFormat for the specified format
	 *
	 * @param format The format we wish to get the AudioFormat for
	 */
	public final static AudioFormat getFormat(String format) {
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
	 * Sets the device used for playing this AudioFormat
	 *
	 * @param device The device to use
	 */
	public void setDevice(AudioOutDevice device) {
		this.device = device;
	}
}
