package pl.asie.libzzt.oop.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.asie.libzzt.oop.conditions.OopCondition;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopCommandIf extends OopCommand {
	private final OopCondition condition;
	private final OopCommand elseCommand;

	public List<OopCommand> getChildren() {
		return elseCommand != null ? List.of(elseCommand) : List.of();
	}
}
