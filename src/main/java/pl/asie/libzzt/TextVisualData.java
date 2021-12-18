package pl.asie.libzzt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class TextVisualData {
	private final int charWidth;
	private final int charHeight;
	private final byte[] charData;
	private final int[] palette;

	public boolean isCharEmpty(int c) {
		for (int i = c * charHeight; i < (c + 1) * charHeight; i++) {
			if (charData[i] != 0) {
				return false;
			}
		}
		return true;
	}

	public boolean isCharFull(int c) {
		for (int i = c * charHeight; i < (c + 1) * charHeight; i++) {
			if (charData[i] != ((byte) 0xFF)) {
				return false;
			}
		}
		return true;
	}
}
