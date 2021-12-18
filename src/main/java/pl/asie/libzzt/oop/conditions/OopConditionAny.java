package pl.asie.libzzt.oop.conditions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.asie.libzzt.oop.OopTile;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopConditionAny extends OopCondition {
	private final OopTile tile;
}
