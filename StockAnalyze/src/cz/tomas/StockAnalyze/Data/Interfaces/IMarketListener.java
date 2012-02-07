package cz.tomas.StockAnalyze.Data.Interfaces;

import cz.tomas.StockAnalyze.Data.Model.Market;

/**
 * @author tomas
 */
public interface IMarketListener {
	
	void onMarketsAvailable(Market[] markets);
}
