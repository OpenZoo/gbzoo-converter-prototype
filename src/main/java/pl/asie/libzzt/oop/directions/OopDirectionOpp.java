package pl.asie.libzzt.oop.directions;

import lombok.Data;
import lombok.EqualsAndHashCode;

public class OopDirectionOpp extends OopDirectionWithChild {
	public OopDirectionOpp(OopDirection child) {
		super(child);
	}
}
