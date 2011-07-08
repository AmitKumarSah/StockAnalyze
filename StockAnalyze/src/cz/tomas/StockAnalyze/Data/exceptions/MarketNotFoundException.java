/*******************************************************************************
 * StockAnalyze for Android
 *     Copyright (C)  2011 Tomas Vondracek.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/**
 * 
 */
package cz.tomas.StockAnalyze.Data.exceptions;

/**
 * @author tomas
 *
 */
@SuppressWarnings("serial")
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
