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
