package cz.tomas.StockAnalyze.Data.exceptions;

public class FailedToGetDataException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4805209634587197867L;

	public FailedToGetDataException() {
		super();
	}

	public FailedToGetDataException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public FailedToGetDataException(String detailMessage) {
		super(detailMessage);
	}

	public FailedToGetDataException(Throwable throwable) {
		super(throwable);
	}

	
}
