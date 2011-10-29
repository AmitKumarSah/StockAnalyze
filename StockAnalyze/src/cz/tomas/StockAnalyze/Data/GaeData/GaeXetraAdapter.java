package cz.tomas.StockAnalyze.Data.GaeData;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import cz.tomas.StockAnalyze.Data.DataProviderAdviser;
import cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Data.exceptions.FailedToGetDataException;
import cz.tomas.StockAnalyze.utils.Markets;
import cz.tomas.StockAnalyze.utils.Utils;

public class GaeXetraAdapter extends GaeDataAdapter {
	
	public GaeXetraAdapter(Context context) {
		super(context);
	}

	public static final String ID = "GAE Xetra Provider";

	@Override
	public List<StockItem> getAvailableStockList()
			throws FailedToGetDataException {
		List<StockItem> stockList;
		try {
			stockList = this.provider.getStockList("de");
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
					Map<String, DayData> data = this.provider.getDayDataSet("de");
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
		DataProviderAdviser adviser = new DataProviderAdviser(true, true, true, Markets.DE);
		return adviser;
	}
}
