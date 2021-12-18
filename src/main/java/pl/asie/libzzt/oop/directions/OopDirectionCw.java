package pl.asie.libzzt.oop.directions;

import lombok.Data;
import lombok.EqualsAndHashCode;

public class OopDirectionCw extends OopDirectionWithChild {
	public OopDirectionCw(OopDirection child) {
		super(child);
	}
}
