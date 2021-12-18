package pl.asie.libzzt.oop.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.asie.libzzt.oop.OopLabelTarget;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopCommandRestore extends OopCommand {
	private final OopLabelTarget target;
}
