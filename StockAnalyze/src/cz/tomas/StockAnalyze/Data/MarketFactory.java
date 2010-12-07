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
	private static Market getCzechMarket() {
		if (czMarket == null)
			czMarket = new Market("PSE", "XPRA", "CZK", "Prague Stock Exchange");
		
		return czMarket;
	}
}
