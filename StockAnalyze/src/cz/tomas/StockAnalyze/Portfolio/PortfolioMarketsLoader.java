package cz.tomas.StockAnalyze.Portfolio;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.StockDataSqlStore;
import cz.tomas.StockAnalyze.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Loader for markets do display in portfolio. This loader groups markets byt currency
 * and skips markets that have no stocks in portfolio.
 *
 * @author tomas
 */
public class PortfolioMarketsLoader extends AsyncTaskLoader<Collection<Market>> {

	private final StockDataSqlStore stockSql;
	private final Portfolio portfolio;

	public PortfolioMarketsLoader(Context context) {
		super(context);

		this.stockSql = StockDataSqlStore.getInstance(context);
		this.portfolio = (Portfolio) context.getApplicationContext().getSystemService(Application.PORTFOLIO_SERVICE);
	}

	@Override
	public Collection<Market> loadInBackground() {
		List<Market> marketsWithItems = null;
		try {
			Map<String, Market> markets = this.stockSql.getMarkets(true);
			marketsWithItems = new ArrayList<Market>(markets.size());
			for (Market market : markets.values()) {
				long count = this.portfolio.getPortfolioItemsCount(market);
				if (count > 0) {
					marketsWithItems.add(market);
				}
			}
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to load markets for portfolio", e);
		}
		return marketsWithItems;
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		this.forceLoad();
	}
}
