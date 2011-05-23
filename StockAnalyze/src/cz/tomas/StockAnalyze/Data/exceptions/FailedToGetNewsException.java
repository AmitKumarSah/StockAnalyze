/*******************************************************************************
 * Copyright (c) 2011 Tomas Vondracek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Tomas Vondracek
 ******************************************************************************/
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
