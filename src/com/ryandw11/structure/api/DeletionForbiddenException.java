package com.ryandw11.structure.api;

public class DeletionForbiddenException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DeletionForbiddenException(String message) {
		super(message);
	}
}
