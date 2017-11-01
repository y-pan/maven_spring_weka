package web.weka.exceptions;

public class OutOfResourceException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OutOfResourceException() {
		super("Out of resource");
	}
	public OutOfResourceException(String msg) {
		super(msg);
	}

}
