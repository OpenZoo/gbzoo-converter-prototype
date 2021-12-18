package pl.asie.libzzt.oop.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.asie.libzzt.oop.directions.OopDirection;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopCommandTry extends OopCommand {
	private final OopDirection direction;
	private final OopCommand elseCommand;

	public List<OopCommand> getChildren() {
		return elseCommand != null ? List.of(elseCommand) : List.of();
	}
}
