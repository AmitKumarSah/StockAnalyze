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
package cz.tomas.StockAnalyze.Data.PsePatriaData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Handler;
import android.util.Log;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.DataProviderAdviser;
import cz.tomas.StockAnalyze.Data.IStockDataProvider;
import cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Data.exceptions.FailedToGetDataException;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * Adapter from PsePatriaDataProvider, that can registered in {@link DataManager}
 * 
 * @author tomas
 *
 */
public class PsePatriaDataAdapter implements IStockDataProvider {

	private final class UpdateTask implements Runnable {
		@Override
		public void run() {
			if (enabled)
				try {
					try {
						for (IStockDataListener listener : eventListeners) {
							listener.OnStockDataUpdateBegin(PsePatriaDataAdapter.this);
						}
					} catch (Exception e) {
						Log.e(Utils.LOG_TAG, "OnStockDataUpdateBegin failed!", e);
					}
					// the market could be closed, so we don't neccessarly get updated data
					if (provider.refresh()) {
						// if refresh proceeded and the market is open, fire the event
						for (IStockDataListener listener : eventListeners) {
							listener.OnStockDataUpdated(PsePatriaDataAdapter.this, null);
						}
					} else {
						for (IStockDataListener listener : eventListeners) {
							listener.OnStockDataNoUpdate(PsePatriaDataAdapter.this);
						}
					}
				} catch (Exception e) {
					Log.e(Utils.LOG_TAG, "Regular update failed!", e);
				}
		}
	}

	private PsePatriaDataProvider provider;
	/*
//	 * time interval between refreshes - in milliseconds
//	 */
//	long refreshInterval = 1000 * 60 * 10;		//Milliseconds

	//Timer timer;
	private boolean enabled;

	private List<IStockDataListener> eventListeners;
	
	/*
	 * mapping between ticker and ISIN (id)
	 * patria does not contain isin information
	 */
	private Map<String, String> tickerIsinMapping;
	
	private Market market;
	
	private UpdateTask updateTask;
	private Handler updateHandler;
	
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
		this.tickerIsinMapping.put("BAANWRUK", "GB00B42CTW68");
		this.tickerIsinMapping.put("BAAORCO", "LU0122624777");
		this.tickerIsinMapping.put("BAAPEGAS", "LU0275164910");
		this.tickerIsinMapping.put("BAATABAK", "CS0008418869");
		this.tickerIsinMapping.put("BAATELEC", "CZ0009093209");
		this.tickerIsinMapping.put("BAAUNIPE", "CZ0009091500");
		this.tickerIsinMapping.put("BAAVIG", "AT0000908504");
		
		market = new Market("PSE", "XPRA", "CZK", this.getDescriptiveName());

		this.eventListeners = new ArrayList<IStockDataListener>();
		this.updateTask = new UpdateTask();
	}
	
	/** 
	 * get last known data from provider
	 * @see cz.tomas.StockAnalyze.Data.IStockDataProvider#getLastData(java.lang.String)
	 */
	@Override
	public DayData getLastData(String ticker) throws FailedToGetDataException {
		PsePatriaDataItem dataItem;
		try {
			dataItem = this.provider.getLastData(ticker);
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to get last known data from provider", e);
			throw new FailedToGetDataException(e);
		}
		DayData data = null;
		if (dataItem != null) {
			Date date = new Date(this.provider.getLastUpdateTime());
			data = new DayData(dataItem.getValue(), dataItem.getPercentableChange(), date, 0, 0, 0, date.getTime());
		}
		return data;
	}

	/**
	 * get (historical) data from specific day
	 * NOT supported for this data provider!
	 * @see cz.tomas.StockAnalyze.Data.IStockDataProvider#getDayData(java.lang.String, java.util.Calendar)
	 */
	@Override
	public DayData getDayData(String ticker, Calendar date) throws IOException {
		// not supported
		return null;
	}

	/** 
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

	/** 
	 * get list of available stocks from data provider
	 * @see cz.tomas.StockAnalyze.Data.IStockDataProvider#getAvailableStockList()
	 */
	@Override
	public List<StockItem> getAvailableStockList() throws FailedToGetDataException {
		List<StockItem> items = new ArrayList<StockItem>();
		Map<String, PsePatriaDataItem> stocks = null;
		try {
			stocks = this.provider.getAvailableStockMap();
		} catch (Exception e) {
			e.printStackTrace();
			throw new FailedToGetDataException(e);
		}

		if (stocks != null)
			for (Entry<String, PsePatriaDataItem> item : stocks.entrySet()) {
				PsePatriaDataItem data = stocks.get(item.getKey());
				if (data == null)
					throw new NullPointerException("No stock data found.");
				String isin = null;
				if (this.tickerIsinMapping.containsKey(item.getKey()))
					isin = this.tickerIsinMapping.get(item.getKey());
				StockItem stockItem = new StockItem(item.getKey(), isin, data.getName(), this.market);
				items.add(stockItem);
			}
		return items;
	}

	/**
	 * id of data provider
	 * @see cz.tomas.StockAnalyze.Data.IStockDataProvider#getId()
	 */
	@Override
	public String getId() {
		return "PSE_PATRIA";
	}

	/**
	 * Descriptive name of data provider
	 * @see cz.tomas.StockAnalyze.Data.IStockDataProvider#getDescriptiveName()
	 */
	@Override
	public String getDescriptiveName() {
		return "Prague Stock Exchange";
	}

	/** 
	 * refresh data from provider 
	 * @see cz.tomas.StockAnalyze.Data.IStockDataProvider#refresh()
	 */
	@Override
	public boolean refresh() {
		boolean result = false;
		try {
			if (this.updateTask != null) {
				this.updateTask.run();
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * get adviser describing this pse provider
	 */
	@Override
	public DataProviderAdviser getAdviser() {
		DataProviderAdviser adviser = new DataProviderAdviser(true, false, false, this.market);
		return adviser;
	}

	@Override
	public void addListener(IStockDataListener listener) {
		this.eventListeners.add(listener);
	}

	@Override
	public void enable(boolean enabled) {
		this.enabled = enabled;
		if (this.updateHandler != null) {
			this.updateHandler.removeCallbacks(this.updateTask);
		}
	}

	@Override
	public Map<Long, Float> getHistoricalPriceSet(String ticker, int timePeriod) {
		throw new UnsupportedOperationException();
	}
}
