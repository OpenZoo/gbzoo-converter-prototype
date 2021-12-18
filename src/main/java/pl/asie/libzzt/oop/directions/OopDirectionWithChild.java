package pl.asie.libzzt.oop.directions;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.asie.libzzt.oop.ChildrenIterable;
import pl.asie.libzzt.oop.conditions.OopCondition;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class OopDirectionWithChild extends OopDirection {
	private final OopDirection child;

	@Override
	public List<OopDirection> getChildren() {
		return child != null ? List.of(child) : List.of();
	}
}
