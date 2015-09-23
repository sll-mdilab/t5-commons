package net.sllmdilab.commons.exceptions;

public class RosettaInitializationException extends T5Exception {

	private static final long serialVersionUID = 1L;

	public RosettaInitializationException() {
	}

	public RosettaInitializationException(String message) {
		super(message);
	}

	public RosettaInitializationException(Throwable cause) {
		super(cause);
	}

	public RosettaInitializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public RosettaInitializationException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
