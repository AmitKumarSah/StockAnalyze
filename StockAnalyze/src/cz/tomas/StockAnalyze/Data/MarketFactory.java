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
package cz.tomas.StockAnalyze.Data;

import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.exceptions.MarketNotFoundException;

/**
 * @author tomas
 * factory for known markets,
 * so far only czech PSE market is supported,
 * instances from this factory are shared across the application
 */
public final class MarketFactory {
	
	static Market czMarket;
	
	public static Market getMarket(String countryCode) {
		if (countryCode.toUpperCase().equals("CZ")) {
			return getCzechMarket();
		}
		else
			throw new MarketNotFoundException(countryCode, "Market for country code " + countryCode + " wasn't found");
	}

	/**
	 * create czech Market instance if it doesn't already exists 
	 */
	public static Market getCzechMarket() {
		if (czMarket == null)
			czMarket = new Market("PSE", "XPRA", "CZK", "Prague Stock Exchange");
		
		return czMarket;
	}
}
