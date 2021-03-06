/* Ogg.java - .ogg playing capabilities
 * Copyright (C) 2001-2005 Fredrik Ehnbom
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
package silence.format.ogg;

import silence.format.*;

import java.io.*;

import com.jcraft.jorbis.*;
import com.jcraft.jogg.*;

/**
 * Pretty much cut and paste from DecoderExample.java from
 * the JOrbis sources.
 *
 * Original author: ymnk <ymnk@jcraft.com>
 *
 * @author Fredrik Ehnbom
 */
public class Ogg
	extends AudioFormat
{
	// ogg
	private SyncState oy = new SyncState();         // sync and verify incoming physical bitstream
	private StreamState os = new StreamState();     // take physical pages, weld into a logical stream of packets
	private Page og = new Page();                   // one Ogg bitstream page.  Vorbis packets are inside
	private Packet op = new Packet();               // one raw packet of data for decode

	// vorbis
	private Info vi = new Info();                   // struct that stores all the static vorbis bitstream settings
	private Comment vc = new Comment();             // struct that stores all the bitstream user comments
	private DspState vd = new DspState();           // central working state for the packet->PCM decoder
	private Block vb = new Block(vd);               // local working space for packet->PCM decode

	private BufferedInputStream in = null;
	private int eos = 0;

	public Ogg() {
	}

	public void load(BufferedInputStream in)
		throws IOException
	{
		this.in = in;

		// Decode setup
		oy.init(); // Now we can read pages


		// grab some data at the head of the stream.  We want the first page
		// (which is guaranteed to be small and only contain the Vorbis
		// stream initial header) We need the first page to get the stream
		// serialno.

		// submit a 4k block to libvorbis' Ogg layer
		int index = oy.buffer(4096);
		byte[] buffer = oy.data;
		int bytes = in.read(buffer, index, 4096);

		oy.wrote(bytes);

		// Get the first page.
		if (oy.pageout(og) != 1) {
			// have we simply run out of data?  If so, we're done.
			if (bytes < 4096) return;

			// error case.  Must not be Vorbis data
			throw new IOException("Input does not appear to be an Ogg bitstream.");
		}

		// Get the serial number and set up the rest of decode.
		// serialno first; use it to set up a logical stream
		os.init(og.serialno());

		// extract the initial header from the first page and verify that the
		// Ogg bitstream is in fact Vorbis data

		// I handle the initial header first instead of just having the code
		// read all three Vorbis headers at once because reading the initial
		// header is an easy way to identify a Vorbis bitstream and it's
		// useful to see that functionality seperated out.

		vi.init();
		vc.init();
		if (os.pagein(og) < 0) {
			// error; stream version mismatch perhaps
			throw new IOException("Error reading first page of Ogg bitstream data.");
		}

		if (os.packetout(op) != 1) {
			// no page? must not be vorbis
			throw new IOException ("Error reading initial header packet.");
		}

		if (vi.synthesis_headerin(vc,op) < 0) {
			// error case; not a vorbis header
			throw new IOException("This Ogg bitstream does not contain Vorbis audio data.");
		}

		// At this point, we're sure we're Vorbis.  We've set up the logical
		// (Ogg) bitstream decoder.  Get the comment and codebook headers and
		// set up the Vorbis decoder

		// The next two packets in order are the comment and codebook headers.
		// They're likely large and may span multiple pages.  Thus we reead
		// and submit data until we get our two pacakets, watching that no
		// pages are missing.  If a page is missing, error out; losing a
		// header page is the only place where missing data is fatal. */

		int i = 0;
		while (i < 2) {
			while (i < 2) {
				int result = oy.pageout(og);
				if (result == 0) break; // Need more data
										// Don't complain about missing or corrupt data yet.  We'll
										// catch it at the packet output phase

				if (result == 1) {
					os.pagein(og);  // we can ignore any errors here
									// as they'll also become apparent
									// at packetout
					while (i < 2) {
						result = os.packetout(op);
						if (result == 0) break;
						if (result == -1) {
							// Uh oh; data at some point was corrupted or missing!
							// We can't tolerate that in a header.  Die.
							throw new IOException("Corrupt secondary header.");
						}
						vi.synthesis_headerin(vc,op);
						i++;
					}
				}
			}
			// no harm in not checking before adding more
			index = oy.buffer(4096);
			buffer = oy.data;
			bytes = in.read(buffer, index, 4096);

			if (bytes == 0 && i < 2){
				throw new IOException("End of file before finding all Vorbis headers!");
			}
			oy.wrote(bytes);
		}

		// Throw the comments plus a few lines about the bitstream we're
		// decoding
		{
/*
			byte[][] ptr = vc.user_comments;
			for (int j = 0; j < ptr.length; j++) {
				if (ptr[j] == null) break;
				System.err.println(new String(ptr[j], 0, ptr[j].length-1));
			}
			System.err.println("\nBitstream is "+vi.channels+" channel, "+vi.rate+"Hz");
			System.err.println("Encoded by: "+new String(vc.vendor, 0, vc.vendor.length-1)+"\n");
*/
		}

		// OK, got and parsed all three headers. Initialize the Vorbis
		//  packet->PCM decoder.
		vd.synthesis_init(vi);  // central decode state
		vb.init(vd);            // local state for most of the decode
								// so multiple block decodes can
								// proceed in parallel.  We could init
								// multiple vorbis_block structures
								// for vd here
		_index = new int[vi.channels];
	}

	private float[][][] _pcm = new float[1][][];
	private int[] _index;


	private final void readData()
		throws IOException
	{
		// read some more...
		int index = oy.buffer(4096);
		byte[] buffer = oy.data;
		int bytes = in.read(buffer,index,4096);

		oy.wrote(bytes);
		if (bytes == 0) eos = 1;
	}

	private boolean haveBreaked = true;
	private int samples;

	private final int updatePcm() {
		if (haveBreaked) {
			int result = os.packetout(op);

			if (result == 0) {
				return 0;
			} else if (result == -1) {
				return -1;
			} else {
				haveBreaked = false;
				// we have a packet.  Decode it
				if (vb.synthesis(op) == 0) { // test for success!
					vd.synthesis_blockin(vb);
				}
				// **pcm is a multichannel double vector.  In stereo, for
				// example, pcm[0] is left, and pcm[1] is right.  samples is
				// the size of each channel.  Convert the float values
				// (-1.<=range<=1.) to whatever PCM format and write it out
				samples = vd.synthesis_pcmout(_pcm, _index);
				if (samples == 0) {
					haveBreaked = true;
				}
			}
		} else {
				samples = vd.synthesis_pcmout(_pcm, _index);
			if (samples == 0) {
				haveBreaked = true;
			}
		}
		return 1;
	}

	private boolean haveDataBreaked = true;

	private final int updateData()
		throws IOException
	{
		if (haveDataBreaked) {
			int result = oy.pageout(og);

			if (result == 0) {
				readData();
				return 0;
			} else if (result == -1) {
				System.err.println("Corrupt or missing data in bitstream; continuing...");
				return -1;
			} else {
				haveDataBreaked = false;
				os.pagein(og);	// can safely ignore errors at
								// this point
			}
		}
		return 1;
	}

	private final int check(int val) {
		if (val > 32767) val = 32767;
		if (val < -32768) val = -32768;
		if (val < 0) val = val | 0x8000;

		return val;
	}

	private float pitch = 0;

	public int read(int[] buffer, int off, int len) {
		try {
mainLoop:
			for (int i = off; i < off+len; ) {
				if (eos != 0) break;

				int result = updateData();
				if (result < 0) {
					if (eos != 0) break;
					continue;
				} else {
					while (true) {
						result = updatePcm();
						if (result == 0) {
							haveDataBreaked = true;
							break;
						} else if (result == -1) {
						} else {
							int bout = (samples < (off+len-i) ? samples : (off+len-i));

							if (samples == 0) continue;

							float z = 0;
							if (vi.channels == 2) { // stereo
								int left = _index[0];
								int right = _index[1];

								while (z < bout && i < (off+len)) {
									buffer[i]    = check((int) (_pcm[0][0][left  + (int) z] * 32767.0)) &65535;
									buffer[i++] |= check((int) (_pcm[0][1][right + (int) z] * 32767.0)) << 16;
									z += pitch;
								}
							} else {
								int left = _index[0];

								while (z < bout && i < (off+len)) {
									int val = check((int) (_pcm[0][0][left  + (int) z] * 32767.0));
									buffer[i]    = val &65535;
									buffer[i++] |= val << 16;
									z += pitch;
								}
							}

							z = z > samples ? samples : z;
							vd.synthesis_read((int) z); // tell libvorbis how
														// many samples we
														// actually consumed

							if (bout == 0) {
								break mainLoop;
							}
						}
					}
				}
			}
			if (eos != 0) {
				// clean up this logical bitstream
				os.clear();

				// ogg_page and ogg_packet structs always point to storage in
				// libvorbis.  They're never freed or manipulated directly
				vb.clear();
				vd.clear();
				vi.clear();  // must be called last

				// OK, clean up the framer
				oy.clear();

				// close InputStream
				in.close();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return -1;
		}

		return len;
	}

	public void close() {
		try {
			in.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void setDevice(org.komplex.audio.AudioOutDevice device) {
		super.setDevice(device);
		pitch = ((float) vi.rate / device.getSampleRate());
	}

	public String toString() {
		return "Ogg Vorbis (JOrbis)";
	}
}
