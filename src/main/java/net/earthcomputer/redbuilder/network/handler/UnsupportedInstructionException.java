package net.earthcomputer.redbuilder.network.handler;

public class UnsupportedInstructionException extends Exception {

	private static final long serialVersionUID = -6225265366510037027L;

	public UnsupportedInstructionException(String desc, Throwable cause) {
		super(desc, cause);
	}

	public UnsupportedInstructionException(String desc) {
		super(desc);
	}

}
