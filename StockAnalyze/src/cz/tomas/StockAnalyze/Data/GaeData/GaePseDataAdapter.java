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

import java.util.Collection;
import java.util.Map;


public class GaePseDataAdapter extends GaeDataAdapter {

	public static final String MARKET_CODE = "cz";
	public static final String ID = "GAE PSE Provider";

	private final DataProviderAdviser adviser = new DataProviderAdviser(true, true, true, MARKET_CODE, false);

	public GaePseDataAdapter(Context context) {
		super(context);
	}

	
	@Override
	public Collection<StockItem> getAvailableStockList(Market market)
			throws FailedToGetDataException {
		Collection<StockItem> stockList;
		try {
			stockList = this.provider.getStockList(market);
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
		return "GAE PSE data provider";
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
					Map<String, DayData> data = this.provider.getDayDataSet(market);
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
