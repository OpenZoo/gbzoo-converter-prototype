package pl.asie.libzzt.oop.commands;

import lombok.Data;
import pl.asie.libzzt.oop.ChildrenIterable;

import java.util.List;

@Data
public abstract class OopCommand implements ChildrenIterable<OopCommand> {
	private Integer position;

	public List<OopCommand> getChildren() {
		return List.of();
	}

	public List<String> getLabels() {
		return List.of();
	}

	public List<String> getFlags() {
		return List.of();
	}
}
