package pl.asie.libzzt.oop.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.asie.libzzt.oop.OopLabelTarget;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopCommandSend extends OopCommand {
	private final OopLabelTarget target;
}
