/**
 * 
 */
package cz.tomas.StockAnalyze.Data.PsePatriaData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cz.tomas.StockAnalyze.Data.IStockDataProvider;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;

/**
 * @author tomas
 *
 */
public class PsePatriaDataProvider {
	
	PsePatriaDataMarketItem currentMarketData;
	
	public PsePatriaDataProvider() {
		this.currentMarketData = new PsePatriaDataMarketItem();
	}
	
	public PsePatriaDataItem getLastData(String ticker) throws Exception {
		//this.currentMarketData.update();
		PsePatriaDataItem stockDataItem = this.currentMarketData.getStock(ticker);
		
		return stockDataItem;
	}
	
	public long getLastUpdateTime() {
		return this.currentMarketData.getLastUpdate();
	}

	public Map<String, PsePatriaDataItem> getAvailableStockMap() throws Exception {
		Map<String, PsePatriaDataItem> stocks = this.currentMarketData.getStocks();
		
		return stocks;
	}

	public boolean refresh() throws Exception {
		this.currentMarketData.update();
		return true;
	}

}
