package cz.tomas.StockAnalyze.Data.GaeData;

import android.content.Context;
import android.util.Log;
import cz.tomas.StockAnalyze.Data.DataProviderAdviser;
import cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Data.exceptions.FailedToGetDataException;
import cz.tomas.StockAnalyze.utils.Utils;

import java.util.List;
import java.util.Map;

public class GaeXetraAdapter extends GaeDataAdapter {

	public static final String MARKET_CODE = "de";
	private final DataProviderAdviser adviser;

	public GaeXetraAdapter(Context context) {
		super(context);
		adviser = new DataProviderAdviser(true, true, true, MARKET_CODE, false);
	}

	public static final String ID = "GAE Xetra Provider";

	@Override
	public List<StockItem> getAvailableStockList(Market market)
			throws FailedToGetDataException {
		List<StockItem> stockList;
		try {
			stockList = this.provider.getStockList(market.getId());
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
		return "GAE Xetra data provider";
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
				// the market could be closed, so we don't necessarily get updated data
				if (provider.refresh()) {
					// if refresh proceeded and the market is open, fire the event
					Map<String, DayData> data = this.provider.getDayDataSet(market.getId());
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
		return adviser;
	}
}
