package cz.tomas.StockAnalyze.Data.GaeData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cz.tomas.StockAnalyze.Data.IStockDataProvider;
import cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.exceptions.FailedToGetDataException;

/**
 * Base class for adpaters working with own backend providers.
 * Subclasses should give concrete info about the concrete provider.
 * @author tomas
 *
 */
public abstract class GaeDataAdapter implements IStockDataProvider {

	protected List<IStockDataListener> eventListeners;
	protected GaeDataProvider provider;
	protected boolean enabled;

	public GaeDataAdapter() {
		super();

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
	public DayData getDayData(String ticker, Calendar date) throws IOException,
			FailedToGetDataException {
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