package cz.tomas.StockAnalyze.Data.GaeData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.util.Log;
import cz.tomas.StockAnalyze.Data.DataProviderAdviser;
import cz.tomas.StockAnalyze.Data.IStockDataProvider;
import cz.tomas.StockAnalyze.Data.MarketFactory;
import cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Data.exceptions.FailedToGetDataException;
import cz.tomas.StockAnalyze.utils.Utils;


public final class GaeDataAdapter implements IStockDataProvider {

	public static final String ID = "GAE PSE Provider";
	
	private List<IStockDataListener> eventListeners;
	private GaeDataProvider provider;
	private boolean enabled;
	
	public GaeDataAdapter() {
		this.eventListeners = new ArrayList<IStockDataListener>();
		this.provider = new GaeDataProvider();
	}
	
	@Override
	public void addListener(IStockDataListener listener) {
		this.eventListeners.add(listener);
	}

	@Override
	public DayData getLastData(String ticker) throws FailedToGetDataException {
		DayData lastData = null;
		try {
			lastData = this.provider.getLastData(ticker);
		} catch (IOException e) {
			throw new FailedToGetDataException(e);
		}
		return lastData;
	}

	@Override
	public DayData getDayData(String ticker, Calendar date) throws IOException, FailedToGetDataException {
		return null;
	}

	/**
	 * get intra day data for current day or last trading if today isn't trading day
	 */
	@Override
	public Map<Long, Float> getIntraDayData(String ticker, Date date) {
		Map<Long, Float> data = null;
		try {
			data = this.provider.getIntraDayData(ticker);
		} catch (Exception e) {
			throw new FailedToGetDataException(e);
		}
		return data;
	}

	@Override
	public List<StockItem> getAvailableStockList()
			throws FailedToGetDataException {
		List<StockItem> stockList;
		try {
			stockList = this.provider.getStockList("cz");
		} catch (Exception e) {
			throw new FailedToGetDataException("failed to get stock list", e);
		}
		return stockList;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getDescriptiveName() {
		return "GAE data provider";
	}

	@Override
	public boolean refresh() {
		if (enabled) {
			try {
				try {
					for (IStockDataListener listener : eventListeners) {
						listener.OnStockDataUpdateBegin(this);
					}
				} catch (Exception e) {
					Log.e(Utils.LOG_TAG, "OnStockDataUpdateBegin failed!", e);
				}
				// the market could be closed, so we don't neccessarly get updated data
				if (provider.refresh()) {
					// if refresh proceeded and the market is open, fire the event
					Map<String, DayData> data = this.provider.getDayDataSet();
					for (IStockDataListener listener : eventListeners) {
						listener.OnStockDataUpdated(this, data);
					}
				} else {
					for (IStockDataListener listener : eventListeners) {
						listener.OnStockDataNoUpdate(this);
					}
				}
				return true;
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "Regular update failed!", e);
			}
		}
		return false;
	}

	@Override
	public DataProviderAdviser getAdviser() {
		DataProviderAdviser adviser = new DataProviderAdviser(true, true, true, MarketFactory.getCzechMarket());
		return adviser;
	}

	@Override
	public void enable(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public Map<Long, Float> getHistoricalPriceSet(String ticker, int timePeriod) throws FailedToGetDataException {
		Map<Long, Float> data = null;
		try {
			data = this.provider.getHistoricalData(ticker, timePeriod);
		} catch (Exception e) {
			throw new FailedToGetDataException("failed to get historical dataset for " + ticker, e);
		}
		return data;
	}

}
