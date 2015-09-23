package net.sllmdilab.commons.exceptions;

public class XmlParsingException extends T5Exception {

	private static final long serialVersionUID = 1L;

	public XmlParsingException() {
	}

	public XmlParsingException(String message) {
		super(message);
	}

	public XmlParsingException(Throwable cause) {
		super(cause);
	}

	public XmlParsingException(String message, Throwable cause) {
		super(message, cause);
	}

	public XmlParsingException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
