package web.weka.exceptions;

public class InvalidFileOrUrlException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidFileOrUrlException() {
		super("Invalid URL or Resource");
	}
	public InvalidFileOrUrlException(String msg) {
		super(msg);
	}

}
