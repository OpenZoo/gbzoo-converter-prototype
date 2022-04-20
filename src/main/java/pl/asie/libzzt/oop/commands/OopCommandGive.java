package pl.asie.libzzt.oop.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.asie.libzzt.oop.OopCounterType;
import pl.asie.libzzt.oop.directions.OopDirection;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopCommandGive extends OopCommand {
	private final OopCounterType counterType;
	private final int amount;
	private final OopCommand elseCommand;
	public List<OopCommand> getChildren() {
		return elseCommand != null ? List.of(elseCommand) : List.of();
	}

	public boolean isTake() {
		return this.amount < 0;
	}
}
