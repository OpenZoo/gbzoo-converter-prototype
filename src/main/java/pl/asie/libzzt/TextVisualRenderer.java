package pl.asie.libzzt;

import java.awt.image.BufferedImage;
import java.util.function.IntFunction;

public class TextVisualRenderer {
	@FunctionalInterface
	public interface ByteGetter {
		int get(int x, int y);
	}

	private final TextVisualData visual;
	private final Platform platform;

	public TextVisualRenderer(TextVisualData visual, Platform platform) {
		this.visual = visual;
		this.platform = platform;
	}

	private ByteGetter applyPreviewToColor(ByteGetter getter, boolean preview) {
		if (preview) {
			return (x, y) -> {
				int c = getter.get(x, y);
				return c == 0x00 ? 0x80 : c;
			};
		} else {
			return getter;
		}
	}

	public BufferedImage render(Board board, boolean preview) {
		return render(board.getWidth(), board.getHeight(), (x, y) -> {
			Element element = board.getElement(x, y);
			if (element == board.getPlatform().getLibrary().getEmpty()) {
				return 32;
			} else if (element.isText()) {
				return board.getColor(x, y);
			} else {
				return element.getCharacter();
			}
		}, applyPreviewToColor((x, y) -> {
			Element element = board.getElement(x, y);
			if (element == board.getPlatform().getLibrary().getEmpty()) {
				return 0x0F;
			} else if (element.isText()) {
				return element.getColor();
			} else {
				return board.getColor(x, y);
			}
		}, preview));
	}

	/* public BufferedImage render(BoardAnimation animation, int frame) {
		return render(animation.getWidth(), animation.getHeight(), (x, y) -> animation.get(x, y, frame), (x, y) -> 0x0F);
	}

	public BufferedImage render(ExColorBoardAnimation animation, int frame) {
		return render(animation.getWidth(), animation.getHeight(), (x, y) -> ExColorBoardAnimationRenderer.elementToChar(animation.get(x, y, frame).getElement()), (x, y) -> animation.get(x, y, frame).getColorIndex());
	} */

	public BufferedImage render(int width, int height, ByteGetter charGetter, ByteGetter colorGetter) {
		int charXInc = (platform.isDoubleWide() ? 2 : 1);
		int charWidth = visual.getCharWidth() * charXInc;
		int charHeight = visual.getCharHeight();
		BufferedImage image = new BufferedImage(width * charWidth, height * charHeight, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int chr = charGetter.get(x, y);
				int col = colorGetter.get(x, y);
				int bgCol = visual.getPalette()[(col >> 4) & 0x0F];
				int fgCol = visual.getPalette()[col & 0x0F];
				byte[] charDataArray = visual.getCharData();
				int charDataOffset = chr * visual.getCharHeight();

				for (int cy = 0; cy < visual.getCharHeight(); cy++) {
					int charData = (int) charDataArray[charDataOffset + cy] & 0xFF;
					int i = 7;
					for (int cx = 0; cx < charWidth; cx += charXInc, i--) {
						int ccol = ((charData & (1 << i)) != 0) ? fgCol : bgCol;
						image.setRGB(x * charWidth + cx, y * charHeight + cy, ccol);
						if (charXInc > 1) {
							image.setRGB(x * charWidth + cx + 1, y * charHeight + cy, ccol);
						}
					}
				}
			}
		}
		return image;
	}
}
