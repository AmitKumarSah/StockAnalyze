package cz.tomas.StockAnalyze.StockList.search;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Data.StockDataSqlStore;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * Task for getting concrete stock item from our server based on ticker.
 * This task will also save the found stock item in database.
 *
 * @author tomas
 */
public abstract class SearchStockItemTask extends AsyncTask<String, Integer, StockItem> {

	private final Context context;
	private final Market market;

	public SearchStockItemTask(Context context, Market market) {
		this.context = context;
		this.market = market;
	}

	@Override
	protected StockItem doInBackground(String... params) {
		if (params == null || params.length !=1) {
			throw new IllegalArgumentException("one ticker expected");
		}
		String ticker = params[0];

		StockItem item = null;
		try {
			item = DataManager.getInstance(context).search(ticker, market);
			if (item != null) {
				StockDataSqlStore sql = StockDataSqlStore.getInstance(context);
				sql.insertStockItem(item);
			}
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to search for stock item from market " + market, e);
		}
		return item;
	}

	@Override
	protected abstract void onPostExecute(StockItem stockItem);
}
