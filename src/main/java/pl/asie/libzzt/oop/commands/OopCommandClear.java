package pl.asie.libzzt.oop.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class OopCommandClear extends OopCommand {
	private final String flag;

	public List<String> getFlags() {
		return List.of(flag);
	}
}
