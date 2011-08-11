package cz.tomas.StockAnalyze.Data.GaeData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cz.tomas.StockAnalyze.Data.DataProviderAdviser;
import cz.tomas.StockAnalyze.Data.IStockDataProvider;
import cz.tomas.StockAnalyze.Data.MarketFactory;
import cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Data.exceptions.FailedToGetDataException;


public final class GaeDataAdapter implements IStockDataProvider {


	private List<IStockDataListener> eventListeners;
	private GaeDataProvider provider;
	
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
		return null;
	}

	@Override
	public DayData getDayData(String ticker, Calendar date) throws IOException, FailedToGetDataException {
		return null;
	}

	@Override
	public DayData[] getIntraDayData(String ticker, Date date, int minuteInterval) {
		return null;
	}

	@Override
	public List<StockItem> getAvailableStockList()
			throws FailedToGetDataException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescriptiveName() {
		return "GAE data provider";
	}

	@Override
	public boolean refresh() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DataProviderAdviser getAdviser() {
		DataProviderAdviser adviser = new DataProviderAdviser(false, true, false, MarketFactory.getCzechMarket());
		return adviser;
	}

	@Override
	public void enable(boolean enabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<Long, Float> getHistoricalPriceSet(String ticker, int timePeriod) throws FailedToGetDataException {
		Map<Long, Float> data = null;
		try {
			data = this.provider.getHistoricalData(ticker, timePeriod);
		} catch (Exception e) {
			throw new FailedToGetDataException(e);
		}
		return data;
	}

}
