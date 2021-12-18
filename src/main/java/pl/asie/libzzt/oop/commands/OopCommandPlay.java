package pl.asie.libzzt.oop.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.asie.libzzt.oop.OopSound;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopCommandPlay extends OopCommand {
	private final OopSound sound;
}
