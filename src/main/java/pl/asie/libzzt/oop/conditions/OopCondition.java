package pl.asie.libzzt.oop.conditions;

import lombok.Data;
import pl.asie.libzzt.oop.ChildrenIterable;

import java.util.List;

@Data
public abstract class OopCondition implements ChildrenIterable<OopCondition> {
	public List<OopCondition> getChildren() {
		return List.of();
	}

	public List<String> getFlags() {
		return List.of();
	}
}
