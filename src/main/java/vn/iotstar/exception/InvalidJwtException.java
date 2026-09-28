package vn.iotstar.exception;

/** JWT sai dinh dang / sai chu ky. */
public class InvalidJwtException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidJwtException(String message) {
		super(message);
	}

	public InvalidJwtException(String message, Throwable cause) {
		super(message, cause);
	}
}
