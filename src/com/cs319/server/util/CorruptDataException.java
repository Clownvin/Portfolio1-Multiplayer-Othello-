package com.cs319.server.util;

import java.io.IOException;

public class CorruptDataException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2198643892554975280L;

	public CorruptDataException(final String message) {
		super(message);
	}
}