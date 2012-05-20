package cz.tomas.StockAnalyze.Portfolio;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.StockDataSqlStore;
import cz.tomas.StockAnalyze.utils.Utils;

import java.util.Collection;

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
		Collection<Market> marketsWithItems = null;
		try {
			this.stockSql.acquireDb(this);

//			Map<String, Market> markets = this.stockSql.getMarkets(true);
			marketsWithItems = this.portfolio.getMarketsWithItems();
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to load markets for portfolio", e);
		} finally {
			this.stockSql.releaseDb(true, this);
		}
		return marketsWithItems;
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		this.forceLoad();
	}
}
