package pl.asie.libzzt.oop;

import lombok.Data;

@Data
public class OopLabelTarget {
	private final String target;
	private final String label;

	public OopLabelTarget(String labelTarget) {
		if (labelTarget.indexOf(':') <= 0) {
			target = "";
			label = labelTarget;
		} else {
			String[] splits = labelTarget.split(":", 2);
			target = splits[0];
			label = splits[1];
		}
	}
}
