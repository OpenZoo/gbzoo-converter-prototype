package pl.asie.libzzt.oop.conditions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.asie.libzzt.oop.directions.OopDirection;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopConditionBlocked extends OopCondition {
	private final OopDirection direction;
}
