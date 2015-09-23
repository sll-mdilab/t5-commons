package net.sllmdilab.commons.exceptions;

public class RosettaLookupException extends T5Exception {

	private static final long serialVersionUID = 1L;

	public RosettaLookupException() {
	}

	public RosettaLookupException(String message) {
		super(message);
	}

	public RosettaLookupException(Throwable cause) {
		super(cause);
	}

	public RosettaLookupException(String message, Throwable cause) {
		super(message, cause);
	}

	public RosettaLookupException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
