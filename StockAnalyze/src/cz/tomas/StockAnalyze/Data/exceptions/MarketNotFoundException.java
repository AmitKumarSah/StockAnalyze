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
public class MarketNotFoundException extends RuntimeException {
	String code;

	/**
	 * code that was used to find the market
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	public MarketNotFoundException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MarketNotFoundException(String code, String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		this.code = code;
	}

	public MarketNotFoundException(String code, String detailMessage) {
		super(detailMessage);
		
		this.code = code;
	}

	public MarketNotFoundException(String code, Throwable throwable) {
		super(throwable);
		this.code = code;
	}
	
	
}
