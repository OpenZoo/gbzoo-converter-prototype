package pl.asie.libzzt.oop.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.asie.libzzt.oop.OopTile;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopCommandBecome extends OopCommand {
	private final OopTile tile;
}
