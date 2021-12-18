package pl.asie.libzzt.oop.conditions;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopConditionNot extends OopCondition {
	private final OopCondition cond;

	@Override
	public List<OopCondition> getChildren() {
		return List.of(cond);
	}
}
