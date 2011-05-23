/**
 * 
 */
package cz.tomas.StockAnalyze.Data.exceptions;

/**
 * @author tomas
 *
 */
public class FailedToGetNewsException extends RuntimeException {
	
	public FailedToGetNewsException() {
		super();
	}

	public FailedToGetNewsException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public FailedToGetNewsException(String detailMessage) {
		super(detailMessage);
	}

	public FailedToGetNewsException(Throwable throwable) {
		super(throwable);
	}
}
