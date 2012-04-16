package cz.tomas.StockAnalyze.Data.GaeData;

import android.content.Context;
import android.util.Log;
import cz.tomas.StockAnalyze.Data.DataProviderAdviser;
import cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Data.StockDataSqlStore;
import cz.tomas.StockAnalyze.Data.exceptions.FailedToGetDataException;
import cz.tomas.StockAnalyze.utils.Utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author tomas
 */
public class GaeUsAdapter extends GaeDataAdapter {

//	public static final String MARKET_CODE_NYSE = "us_nyse";
//	public static final String MARKET_CODE_NASDAQ = "us_nasdaq";
	public static final String MARKET_CODE = "us";
	public static final String ID = "GAE US Provider";

	private final DataProviderAdviser adviser;
	private final Context context;

	public GaeUsAdapter(Context context) {
		super(context);
		this.context = context;
//		HashSet<String> markets = new HashSet<String>(2);
//		markets.add(MARKET_CODE_NASDAQ);
//		markets.add(MARKET_CODE_NYSE);
		adviser = new DataProviderAdviser(true, true, true, MARKET_CODE, true);
	}

	@Override
	public List<StockItem> getAvailableStockList(Market market) throws FailedToGetDataException {
		try {
			return this.provider.getStockList(market.getCountry());
		} catch (IOException e) {
			throw new FailedToGetDataException("failed to get stock list", e);
		}
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getDescriptiveName() {
		return "GAE provider for nasdaq & nyse";
	}

	@Override
	public boolean refresh(Market market) {
		if (enabled) {
			try {
				try {
					for (IStockDataListener listener : eventListeners) {
						listener.OnStockDataUpdateBegin(this);
					}
				} catch (Exception e) {
					Log.e(Utils.LOG_TAG, "OnStockDataUpdateBegin failed!", e);
				}
				if (provider.refresh()) {
					StockDataSqlStore sql = StockDataSqlStore.getInstance(this.context);
					Map<String, StockItem> stocks = sql.getStockItems(market, null);

					if (stocks == null || stocks.size() == 0) {
						return false;
					}
					Map<String, DayData> data = this.provider.getYahooDayDataSet(stocks.values());
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
				Log.e(Utils.LOG_TAG, "Regular update failed for " + market, e);
			}
		}
		return false;
	}

	@Override
	public StockItem search(String ticker, Market market) throws IOException {
		return provider.search(ticker, market);
	}

	@Override
	public DataProviderAdviser getAdviser() {
		return adviser;
	}
}
