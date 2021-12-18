package pl.asie.libzzt.oop.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.asie.libzzt.oop.directions.OopDirection;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopCommandDirection extends OopCommand {
	private final OopDirection direction;
}
