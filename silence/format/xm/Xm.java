/* Xm.java - Xm format handling class
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
package silence.format.xm;

import java.io.*;
import java.net.URL;

import silence.format.AudioFormat;
import silence.format.xm.data.*;

/**
 * The general xm class
 *
 * @author Fredrik Ehnbom
 */
public class Xm
	extends AudioFormat
{

	ModulePlayer modulePlayer = new ModulePlayer();

	public Xm() {
	}

	/**
	 * Load the file into memory
 	 *
	 * @param is The InputStream to read the file from
	 */
	public void load(BufferedInputStream in)
		throws IOException
	{
		XmLoader xl = new XmLoader();
		Module module = new Module();

		xl.load(in, module);
		modulePlayer.setModule(module);
	}


	/**
	 * Play...
	 */
	public int read(int[] buffer, int off, int len) {
		modulePlayer.read(buffer, off, len);
		return len;
	}


	public void setDevice(org.komplex.audio.AudioOutDevice device) {
		super.setDevice(device);
		modulePlayer.setDeviceSampleRate(device.getSampleRate());
	}

	public void close() {}

	public String toString() {
		return "Extended Module";
	}
}
