/* AudioFormat.java - The basic AudioFormat class
 * Copyright (C) 2000-2001 Fredrik Ehnbom
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
 * The basic class for AudioFormats. If you want to add support
 * for, lets say, .mp3-files this is the class to extend.
 *
 * @author Fredrik Ehnbom
 * @version $Id: AudioFormat.java,v 1.4 2001/01/04 18:53:46 fredde Exp $
 */
public abstract class AudioFormat
	implements PullAudioSource
{

	/** List of formats */
	private static Hashtable flist = new Hashtable();

	/** the samplerate for the device used */
	public int deviceSampleRate = 0;

	/** play loud? */
	protected boolean playLoud = true;

	/** number of channels */
	protected int channels = 0;

	static {
		// put all supported formats to the format list
		flist.put(".xm", "org.gjt.fredde.silence.format.xm.Xm");
		flist.put(".au", "org.gjt.fredde.silence.format.au.Au");
		flist.put(".ogg", "org.gjt.fredde.silence.format.ogg.Ogg");
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
			load(new BufferedInputStream(u.openStream()));
		} else {
			load(new BufferedInputStream(new FileInputStream(file)));
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
	public abstract void close() throws IOException;

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
		playLoud = !(device instanceof org.komplex.audio.device.NoSoundDevice);
		deviceSampleRate = device.getSampleRate();
		channels = device.getChannels();
	}
}
/*
 * ChangeLog:
 * $Log: AudioFormat.java,v $
 * Revision 1.4  2001/01/04 18:53:46  fredde
 * added ogg and the close method
 *
 * Revision 1.3  2000/12/21 17:15:00  fredde
 * added protected field "channels", made load(is) public
 *
 * Revision 1.2  2000/09/29 19:38:43  fredde
 * Added some more javadoc, removed
 * unused stuff and made some methods final
 *
 * Revision 1.1.1.1  2000/09/25 16:34:34  fredde
 * initial commit
 *
 */
