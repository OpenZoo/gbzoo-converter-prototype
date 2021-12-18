package pl.asie.libzzt.oop.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.asie.libzzt.oop.directions.OopDirection;
import pl.asie.libzzt.oop.OopTile;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopCommandPut extends OopCommand {
	private final OopDirection direction;
	private final OopTile tile;
}
