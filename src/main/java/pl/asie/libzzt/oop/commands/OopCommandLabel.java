package pl.asie.libzzt.oop.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopCommandLabel extends OopCommand {
	private final String label;
	private final boolean zapped;

	@Override
	public List<String> getLabels() {
		return List.of(label);
	}
}
