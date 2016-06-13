package com.cs319.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public final class StreamUtils {
	public static final byte EOF_CHARACTER = -1;

	public static final int MAX_PACKET_BYTES = 1000;

	public static byte[] readBlockFromStream(final InputStream input)
			throws EndOfStreamReachedException, IOException, CorruptDataException {
		//Read checksum from stream
		byte checksum = (byte) input.read();
		byte[] buffer = new byte[4];
		int totalEOFChars = 0;
		//Read 4 bytes (which are the block length, if they were sent using the proper means)
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = (byte) input.read();
			if (buffer[i] == EOF_CHARACTER) {
				totalEOFChars++;
			}
		}
		if (totalEOFChars == buffer.length) {
			throw new EndOfStreamReachedException("The end of the stream has been reached.");
		}
		//Construct size from the buffer using bitwise ops
		int size = (buffer[0] << 24) | (buffer[1] << 16) | (buffer[2] << 8) | (buffer[3]);
		if (size < 0) {
			throw new NegativeArraySizeException("Negative block size read from stream.");
		}
		if (size >= MAX_PACKET_BYTES) {
			throw new ArrayIndexOutOfBoundsException("Block size read from stream was too large.");
		}
		buffer = new byte[size];
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = (byte) input.read();
		}
		int total = 0;
		totalEOFChars = 0;
		for (byte b : buffer) {
			total += b;
			if (b == EOF_CHARACTER) {
				totalEOFChars++;
			}
		}
		// >> 1 is equivalent to integer divide by two.
		if (totalEOFChars > buffer.length >> 1) { // Mostly EOF characters.
			throw new EndOfStreamReachedException("The end of the stream has been reached.");
		}
		if (total % 0xF != checksum) {
			throw new CorruptDataException("Block read from stream was corrupt.");
		}
		return buffer;
	}

	public static void writeBlockToStream(final OutputStream output, final byte[] block) throws IOException {
		byte[] lengthBytes = { (byte) ((block.length >> 24) & 0xFF), (byte) ((block.length >> 16) & 0xFF),
				(byte) ((block.length >> 8) & 0xFF), (byte) (block.length & 0xFF) };
		int blockTotal = 0;
		for (byte b : block) {
			blockTotal += b;
		}
		output.write((byte) (blockTotal % 0xF)); // Write checksum to stream.
		output.write(lengthBytes);
		output.write(block);
		output.flush();
	}

	private StreamUtils() {
		// Since this is a static utility class, no need to instantiate
	}
}