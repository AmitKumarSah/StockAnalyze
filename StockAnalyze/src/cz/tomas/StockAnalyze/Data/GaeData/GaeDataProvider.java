package cz.tomas.StockAnalyze.Data.GaeData;

import android.content.Context;
import android.text.TextUtils;
import com.google.gson.JsonSyntaxException;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.rest.Infrastructure;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public final class GaeDataProvider {

	private static final String[] TIME_PERIODS = { "", "1D", "1W", "1M", "3M", "6M", "1Y" };

	private final Infrastructure infrastructure;
	
	GaeDataProvider(Context context) {
		this.infrastructure = new Infrastructure();
	}

	/**
	 * get day data for whole market
	 * 
	 * @param market market to get get the data from
	 * @return map of stock id vs. dayData
	 * @throws JsonSyntaxException
	 * @throws IOException
	 */
	Map<String, DayData> getDayDataSet(Market market) throws JsonSyntaxException, IOException {
		if (market == null || TextUtils.isEmpty(market.getId())) {
			throw new NullPointerException("country code can't be empty!");
		}
		return infrastructure.getDataSet(market);
	}

	Map<String, DayData> getDayDataSet(Collection<StockItem> stocks) throws IOException {
		if (stocks == null || stocks.size() == 0) {
			throw new NullPointerException("stocks can't be empty!");
		}
		return infrastructure.getDataSet(stocks);
	}

	/**
	 * get last available data for given {@link StockItem#getTicker()}
	 * @param ticker stock ticker
	 * @return day data
	 * @throws IOException
	 */
	DayData getLastData(String ticker) throws IOException {
		throw new UnsupportedOperationException();
	}

	public StockItem getStock(String ticker) throws IOException {
		return this.infrastructure.getStock(ticker);
	}
	
	/**
	 * get list of stocks for given country code
	 * @return collection of stock items for given market
	 * @throws JsonSyntaxException
	 * @throws IOException
	 */
	Collection<StockItem> getStockList(Market market) throws JsonSyntaxException, IOException {
		if (market != null && TextUtils.isEmpty(market.getId())) {
			throw new NullPointerException("market can't be empty!");
		}
		return this.infrastructure.getStockList(market);
	}
	
	Map<Long, Float> getHistoricalData(String ticker, int timePeriod) throws IOException {
		if (TextUtils.isEmpty(ticker)) {
			throw new NullPointerException("stock ticker can't be empty!");
		}

		return this.infrastructure.getChartData(ticker, TIME_PERIODS[timePeriod]);
	}

	Map<Long, Float> getIntraDayData(String ticker) throws IOException {
		if (TextUtils.isEmpty(ticker)) {
			throw new NullPointerException("stock ticker can't be null!");
		}
		return this.infrastructure.getChartData(ticker, "1D");
	}

	public boolean refresh() {
		
		return true;
	}
}
