package net.sllmdilab.commons.exceptions;

public class T5Exception extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public T5Exception() {
	}

	public T5Exception(String message) {
		super(message);
	}

	public T5Exception(Throwable cause) {
		super(cause);
	}

	public T5Exception(String message, Throwable cause) {
		super(message, cause);
	}

	public T5Exception(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
