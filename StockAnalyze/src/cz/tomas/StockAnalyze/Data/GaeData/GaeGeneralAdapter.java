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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author tomas
 */
public class GaeGeneralAdapter extends GaeDataAdapter {

	private static final String ID = "GAE general provider";
	private final DataProviderAdviser adviser;
	private final GaeDataProvider provider;

	public GaeGeneralAdapter(Context context) {
		super(context);
		HashSet<String> marketCodes = new HashSet<String>();
		marketCodes.add("pl");
		marketCodes.add("gb");
		marketCodes.add("hu");
		marketCodes.add("sw");
		marketCodes.add("jp");
		marketCodes.add("rus");
		adviser = new DataProviderAdviser(true, true, true, false, marketCodes);
		provider = new GaeDataProvider(context);
	}

	@Override
	public List<StockItem> getAvailableStockList(Market market) throws FailedToGetDataException {
		try {
			return this.provider.getStockList(market.getId());
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
		return "GAE general";
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
