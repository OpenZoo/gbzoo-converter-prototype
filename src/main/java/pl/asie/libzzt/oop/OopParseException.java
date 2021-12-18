package pl.asie.libzzt.oop;

public class OopParseException extends RuntimeException {
	public OopParseException(OopProgramParser p, String s) {
		super(s);
	}
}
