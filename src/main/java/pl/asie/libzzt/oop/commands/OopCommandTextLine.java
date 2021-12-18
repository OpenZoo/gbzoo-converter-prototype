package pl.asie.libzzt.oop.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopCommandTextLine extends OopCommand {
	public enum Type {
		REGULAR,
		CENTERED,
		HYPERLINK,
		EXTERNAL_HYPERLINK
	}

	private final Type type;
	private final String destination;
	private final String message;
}
