package json;
public class ParserException extends RuntimeException {
	public ParserException(String message) {
		super(message);
	}
	public ParserException(String message, Throwable cause) {
		super(message, cause);
	}
	public ParserException(String format, Object... args) {
		super(String.format(format, args));
	}
	public ParserException(String format, Throwable cause, Object... args) {
		super(String.format(format, args), cause);
	}
}
