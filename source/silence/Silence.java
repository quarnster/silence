/* Silence.java - An example player for Silence
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

package silence;

import silence.devices.*;
import silence.devices.midas.*;

/**
 * An example player for Silence
 * This is <strong>not</strong> meant to be used in your programs.
 * Make your own instead.
 * @author Fredrik Ehnbom
 * @version $Id: Silence.java,v 1.1 2000/04/29 10:21:19 quarn Exp $
 */
public class Silence {

	/**
	 * The AudioDevice to use for playing the AudioFormat 
	 */
	private AudioDevice audioDevice = new MidasDevice() {
		// This is how you redefine the sync method.
		public void sync(int eff) {
			System.out.println("Midas wants to sync... (effect num: " + eff + ")");
		}
	};

	/**
	 * Creates a new Silence player.
	 * @param file The file to play
	 */
	public Silence(String file) {
		try {
			audioDevice.init(file, true);
			audioDevice.play();
		} catch (AudioException me) {
			me.printStackTrace();
			audioDevice.stop();
			audioDevice.close();
		}

	}

	public static void main(String args[]) {
		if (args.length != 1) {
			System.out.println("usage: java silence.Silence <soundfile>");
		} else {
			Silence c = new Silence(args[0]);
		}
	}
}
/*
 * ChangeLog:
 * $Log: Silence.java,v $
 * Revision 1.1  2000/04/29 10:21:19  quarn
 * Initial revision
 *
 */
