package pl.asie.gbzooconv;

public class BoardParseException extends RuntimeException {
	public BoardParseException(String s) {
		super(s);
	}

	public BoardParseException(String s, Exception e) {
		super(s, e);
	}
}
