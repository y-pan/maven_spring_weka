package web.weka.exceptions;

public class InvalidFileException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidFileException() {
		super("Invalid URL");
	}
	public InvalidFileException(String msg) {
		super(msg);
	}

}
