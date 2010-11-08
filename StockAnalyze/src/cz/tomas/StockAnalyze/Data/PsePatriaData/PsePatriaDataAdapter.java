/**
 * 
 */
package cz.tomas.StockAnalyze.Data.PsePatriaData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cz.tomas.StockAnalyze.Data.IStockDataProvider;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;

/**
 * @author tomas
 *
 * Adapter from PsePatriaDataProvider
 */
public class PsePatriaDataAdapter implements IStockDataProvider {

	PsePatriaDataProvider provider;
	
	/*
	 * mapping between ticker and ISIN (id)
	 * patria does not contain isin information
	 */
	Map<String, String> tickerIsinMapping;
	
	public PsePatriaDataAdapter() {
		this.provider = new PsePatriaDataProvider();
		
		this.tickerIsinMapping = new HashMap<String, String>();
		this.tickerIsinMapping.put("PX", "PX");
		this.tickerIsinMapping.put("BAAAAA", "NL0006033375");
		this.tickerIsinMapping.put("BAACETV", "BMG200452024");
		this.tickerIsinMapping.put("BAACEZ", "CZ0005112300");
		this.tickerIsinMapping.put("BAAECM", "LU0259919230");
		this.tickerIsinMapping.put("BAAERBAG", "AT0000652011");
		this.tickerIsinMapping.put("BAAFOREG", "NL0009604859");
		this.tickerIsinMapping.put("BAAKOMB", "CZ0008019106");
		this.tickerIsinMapping.put("BAAKITDG", "US4824702009");
		this.tickerIsinMapping.put("BAANWR", "NL0006282204");
		this.tickerIsinMapping.put("BAAKORCO", "LU0122624777");
		this.tickerIsinMapping.put("BAAPEGAS", "LU0275164910");
		this.tickerIsinMapping.put("BAATABAK", "CS0008418869");
		this.tickerIsinMapping.put("BAAKTELEC", "CZ0009093209");
		this.tickerIsinMapping.put("BAAUNIPE", "CZ0009091500");
		this.tickerIsinMapping.put("BAAVIG", "AT0000908504");
	}
	
	/* 
	 * get last known data from provider
	 * @see cz.tomas.StockAnalyze.Data.IStockDataProvider#getLastData(java.lang.String)
	 */
	@Override
	public DayData getLastData(String ticker) throws IOException {
		PsePatriaDataItem stockItem = this.provider.getLastData(ticker);
		DayData data = null;
		if (stockItem != null) {
			Date date = new Date(this.provider.getLastUpdateTime());
			data = new DayData(stockItem.getValue(), stockItem.getPercentableChange(), date, 0);
		}
		return data;
	}

	/* 
	 * get (historical) data from specific day
	 * NOT supported for this data provider
	 * @see cz.tomas.StockAnalyze.Data.IStockDataProvider#getDayData(java.lang.String, java.util.Calendar)
	 */
	@Override
	public DayData getDayData(String ticker, Calendar date) throws IOException {
		// not supported
		return null;
	}

	/* 
	 * get data from within the one day
	 * NOT supported by this provider
	 * @see cz.tomas.StockAnalyze.Data.IStockDataProvider#getIntraDayData(java.lang.String, java.util.Date, int)
	 */
	@Override
	public DayData[] getIntraDayData(String ticker, Date date,
			int minuteInterval) {
		// not supported
		return null;
	}

	/* 
	 * get list of available stocks from data provider
	 * @see cz.tomas.StockAnalyze.Data.IStockDataProvider#getAvailableStockList()
	 */
	@Override
	public List<StockItem> getAvailableStockList() {
		List<StockItem> items = new ArrayList<StockItem>();
		Map<String, PsePatriaDataItem> stocks = this.provider.getAvailableStockMap();

		if (stocks != null)
			for (Entry<String, PsePatriaDataItem> item : stocks.entrySet()) {
				PsePatriaDataItem data = stocks.get(item.getKey());
				if (data == null)
					throw new NullPointerException("No stock data found.");
				String isin = null;
				if (this.tickerIsinMapping.containsKey(item.getKey()))
					isin = this.tickerIsinMapping.get(item.getKey());
				StockItem stockItem = new StockItem(item.getKey(), isin, data.getName(), "BCPP");
				items.add(stockItem);
			}
		return items;
	}

	/* id of data provider
	 * @see cz.tomas.StockAnalyze.Data.IStockDataProvider#getId()
	 */
	@Override
	public String getId() {
		return "PSE_PATRIA";
	}

	/* Descriptive name of data provider
	 * @see cz.tomas.StockAnalyze.Data.IStockDataProvider#getDescriptiveName()
	 */
	@Override
	public String getDescriptiveName() {
		return "Prague Stock Exchange";
	}

	/* 
	 * refresh data from provider 
	 * @see cz.tomas.StockAnalyze.Data.IStockDataProvider#refresh()
	 */
	@Override
	public boolean refresh() {
		return this.provider.refresh();
	}

}
