package pl.asie.libzzt.oop.conditions;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopConditionFlag extends OopCondition {
	private final String flag;

	@Override
	public List<String> getFlags() {
		return List.of(flag);
	}
}
