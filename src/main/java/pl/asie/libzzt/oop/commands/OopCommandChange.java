package pl.asie.libzzt.oop.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.asie.libzzt.oop.OopTile;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopCommandChange extends OopCommand {
	private final OopTile tileFrom, tileTo;
}
