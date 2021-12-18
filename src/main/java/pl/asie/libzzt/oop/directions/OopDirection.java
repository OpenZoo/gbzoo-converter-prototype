package pl.asie.libzzt.oop.directions;

import lombok.Data;
import pl.asie.libzzt.oop.ChildrenIterable;

import java.util.List;

@Data
public abstract class OopDirection implements ChildrenIterable<OopDirection> {
	@Override
	public List<OopDirection> getChildren() {
		return List.of();
	}
}
