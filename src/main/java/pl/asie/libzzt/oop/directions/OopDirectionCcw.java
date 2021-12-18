package pl.asie.libzzt.oop.directions;

import lombok.Data;
import lombok.EqualsAndHashCode;

public class OopDirectionCcw extends OopDirectionWithChild {
	public OopDirectionCcw(OopDirection child) {
		super(child);
	}
}
