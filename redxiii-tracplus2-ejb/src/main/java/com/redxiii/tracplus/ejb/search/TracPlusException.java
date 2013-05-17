package com.redxiii.tracplus.ejb.search;

/**
 * @author Daniel
 *
 */
public class TracPlusException extends IllegalStateException {

	private static final long serialVersionUID = 1L;

	public static enum Code {
		UNKNOWN
	}
	
	private Code code;

	public TracPlusException(Code code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}
	
	public Code getCode() {
		return code;
	}
	
}
