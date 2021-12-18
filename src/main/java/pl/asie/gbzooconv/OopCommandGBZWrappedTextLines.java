package pl.asie.gbzooconv;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.davidmoten.text.utils.WordWrap;
import pl.asie.libzzt.oop.commands.OopCommand;
import pl.asie.libzzt.oop.commands.OopCommandTextLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopCommandGBZWrappedTextLines extends OopCommand {
	private List<OopCommandTextLine> lines;
	private int lineCount;

	public OopCommandGBZWrappedTextLines(List<OopCommandTextLine> originalLines, int wordWrapWidth) {
		this.lines = new ArrayList<>();
		this.lineCount = originalLines.size();

		List<OopCommandTextLine> lineBuffer = new ArrayList<>();
		for (OopCommandTextLine line : originalLines) {
			// If the line is empty, make it stand alone.
			if (line.getType() != OopCommandTextLine.Type.HYPERLINK && line.getMessage().isEmpty()) {
				addLineBuffer(lineBuffer, wordWrapWidth);
				lineBuffer.clear();
				lineBuffer.add(line);
				addLineBuffer(lineBuffer, wordWrapWidth);
				lineBuffer.clear();
			}

			boolean lineCompatible = false;
			if (lineBuffer.isEmpty()) {
				lineCompatible = true;
			} else {
				OopCommandTextLine currLine = lineBuffer.get(0);
				if (Objects.equals(currLine.getType(), line.getType()) && Objects.equals(currLine.getDestination(), line.getDestination())) {
					lineCompatible = true;
				}
			}
			if (!lineCompatible) {
				addLineBuffer(lineBuffer, wordWrapWidth);
				lineBuffer.clear();
			}
			lineBuffer.add(line);
		}

		if (!lineBuffer.isEmpty()) {
			addLineBuffer(lineBuffer, wordWrapWidth);
		}
	}

	private void addLineBuffer(List<OopCommandTextLine> buffer, int wordWrapWidth) {
		if (buffer.isEmpty()) {
			return;
		}

		OopCommandTextLine currLine = buffer.get(0);
		String fullText = buffer.stream().map(OopCommandTextLine::getMessage).collect(Collectors.joining(" "));
		if (currLine.getType() == OopCommandTextLine.Type.EXTERNAL_HYPERLINK) {
			throw new RuntimeException("Unsupported text line type: " + currLine.getType());
		} else if (currLine.getType() == OopCommandTextLine.Type.HYPERLINK) {
			wordWrapWidth -= 3;
		}

		if (fullText.isBlank()) {
			lines.add(createTextLine(currLine.getType(), currLine.getDestination(), "", wordWrapWidth));
		} else {
			try {
				for (String s : WordWrap.from(fullText).maxWidth(wordWrapWidth).wrapToList()) {
					lines.add(createTextLine(currLine.getType(), currLine.getDestination(), s, wordWrapWidth));
				}
			} catch (IllegalArgumentException e) {
				fullText = buffer.stream().map(OopCommandTextLine::getMessage).map(String::strip).collect(Collectors.joining(" "));
				try {
					for (String s : WordWrap.from(fullText).maxWidth(wordWrapWidth).wrapToList()) {
						lines.add(createTextLine(currLine.getType(), currLine.getDestination(), s, wordWrapWidth));
					}
				} catch (IllegalArgumentException ee) {
					throw new RuntimeException(fullText, ee);
				}
			}
		}
	}

	private OopCommandTextLine createTextLine(OopCommandTextLine.Type type, String destination, String s, int wordWrapWidth) {
		if (type == OopCommandTextLine.Type.CENTERED) {
			int offset = (wordWrapWidth - s.length()) / 2;
			if (offset > 0) {
				s = " ".repeat(offset) + s;
			}
		}
		return new OopCommandTextLine(type, destination, s);
	}
}
