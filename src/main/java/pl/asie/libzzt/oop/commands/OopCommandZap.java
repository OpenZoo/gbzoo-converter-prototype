package pl.asie.libzzt.oop.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.asie.libzzt.oop.OopLabelTarget;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopCommandZap extends OopCommand {
	private final OopLabelTarget target;
}
