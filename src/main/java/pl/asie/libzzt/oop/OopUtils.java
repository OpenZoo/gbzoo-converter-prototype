package pl.asie.libzzt.oop;

import pl.asie.libzzt.Board;
import pl.asie.libzzt.Stat;

import java.util.stream.Stream;

public final class OopUtils {
	private OopUtils() {

	}

	public static <T extends ChildrenIterable<T>> Stream<T> allChildren(Stream<T> commandStream) {
		return commandStream.flatMap(c -> Stream.concat(Stream.of(c), allChildren(c.getChildren().stream())));
	}

	public static String stripChars(String value) {
		StringBuilder newValue = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
			int codePoint = value.codePointAt(i);
			if (codePoint >= 'A' && codePoint <= 'Z') {
				newValue.appendCodePoint(codePoint);
			} else if (codePoint >= '0' && codePoint <= '9') {
				newValue.appendCodePoint(codePoint);
			} else if (codePoint >= 'a' && codePoint <= 'z') {
				newValue.appendCodePoint(codePoint + 'A' - 'a');
			}
		}
		return newValue.toString();
	}
}
