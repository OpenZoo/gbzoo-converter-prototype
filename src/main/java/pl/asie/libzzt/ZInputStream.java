package pl.asie.libzzt;

import lombok.Getter;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ZInputStream extends FilterInputStream {
	@Getter
	private final Platform platform;

	public ZInputStream(InputStream in, Platform platform) {
		super(in);
		this.platform = platform;
	}

	public int readPByte() throws IOException {
		return read() & 0xFF;
	}

	public boolean readPBoolean() throws IOException {
		return readPByte() != 0;
	}

	public int readPShort() throws IOException {
		int lsb = read() & 0xFF;
		int value = lsb | ((read() & 0xFF) << 8);
		if (value >= 32768) {
			return value - 65536;
		} else {
			return value;
		}
	}

	public String readPString(int length) throws IOException {
		int textLength = Math.min(length, readPByte());
		int remLength = length - textLength;
		byte[] data = new byte[textLength];
		if (read(data) < textLength) {
			throw new IOException("Not enough data in buffer!");
		}
		if (skip(remLength) < remLength) {
			throw new IOException("Not enough data in buffer!");
		}
		return new String(data, StandardCharsets.ISO_8859_1);
	}
}
