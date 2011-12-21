package cz.tomas.StockAnalyze.Portfolio;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Interfaces.IUpdateDateChangedListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.PortfolioItem;
import cz.tomas.StockAnalyze.Data.Model.PortfolioSum;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Portfolio.Portfolio.IPortfolioListener;
import cz.tomas.StockAnalyze.utils.Utils;

public final class PortfolioLoader extends AsyncTaskLoader<PortfolioListData> implements IUpdateDateChangedListener, OnSharedPreferenceChangeListener, IPortfolioListener {

	private final DataManager dataManager;
	private final Portfolio portfolio;
	private final Market market;
	private final SharedPreferences pref;

	public PortfolioLoader(Context context, Market market) {
		super(context);
		this.dataManager = (DataManager) context.getApplicationContext().getSystemService(Application.DATA_MANAGER_SERVICE);
		this.portfolio = (Portfolio) context.getApplicationContext().getSystemService(Application.PORTFOLIO_SERVICE);
		this.market = market;
		this.pref = this.getContext().getSharedPreferences(Utils.PREF_NAME, 0);
	}

	@Override
	public PortfolioListData loadInBackground() {
		List<PortfolioItem> items = null;
		Map<PortfolioItem, DayData> datas = new LinkedHashMap<PortfolioItem, DayData>();
		Map<String, StockItem> stockItems = new LinkedHashMap<String, StockItem>();
		
		double totalValueSum = 0;
		double totalAbsChangeSum = 0;
		double totalInvestedSum = 0;
		
		try {
			dataManager.acquireDb(this.getClass().getName());
			try {
				items = portfolio.getGroupedPortfolioItems(market);
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "failed to get groupped portfolio items", e);
			}
			try {
				if (items != null) {
					final boolean includeFee = pref.getBoolean(Utils.PREF_PORTFOLIO_INCLUDE_FEE, true);
					// get day data for each stock and save it
					for (PortfolioItem portfolioItem : items) {
						DayData dayData = dataManager.getLastOfflineValue(portfolioItem.getStockId());
						datas.put(portfolioItem, dayData);
						
						// load also needed stock items
						StockItem stockItem = dataManager.getStockItem(portfolioItem.getStockId(), portfolioItem.getMarketId());
						stockItems.put(portfolioItem.getStockId(), stockItem);

						double itemValue = portfolioItem.getCurrentStockCount() * dayData.getPrice();
						itemValue = Math.abs(itemValue);
						if (includeFee ) {
							itemValue -= portfolioItem.getBuyFee();
							itemValue -= portfolioItem.getSellFee();
						}
						
						// invested value is here because item value might be 0,
						// because user bought something and also sold it - therefore
						// we need to know invested money so we can calculate percentual change for whole portfolio
						final double investedValue = portfolioItem.getInvestedValue(includeFee);
						final double[] changes = new double[2];
						portfolioItem.calculateChanges(dayData.getPrice(), includeFee, changes);
						totalValueSum += itemValue;	// count short positions as positive so we have their value
						totalAbsChangeSum += changes[1];
						totalInvestedSum += investedValue;
					}
				}
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "Failed to get stock day data.", e);
			}
		} finally {
			dataManager.releaseDb(true, this.getClass().getName());
		}

		double totalPercChange = 0.0;
		if (totalInvestedSum > 0) {
			totalPercChange = (totalAbsChangeSum / totalInvestedSum) * 100;
		}

		PortfolioSum portfolioSummary = new PortfolioSum(totalValueSum, totalAbsChangeSum, totalPercChange);
		PortfolioListData data = new PortfolioListData(datas, stockItems, portfolioSummary);
		return data;
	}

	@Override
	public void registerListener(int id, OnLoadCompleteListener<PortfolioListData> listener) {
		this.dataManager.addUpdateChangedListener(this);
		this.pref.registerOnSharedPreferenceChangeListener(this);
		this.portfolio.addPortfolioListener(this);
		super.registerListener(id, listener);
	}

	@Override
	public void unregisterListener(OnLoadCompleteListener<PortfolioListData> listener) {
		this.dataManager.removeUpdateChangedListener(this);
		this.pref.unregisterOnSharedPreferenceChangeListener(this);
		this.portfolio.removePortfolioListener(this);
		super.unregisterListener(listener);
	}

	@Override
	protected void onStartLoading() {
		this.forceLoad();
	}
	
	
	@Override
	public void OnLastUpdateDateChanged(long updateTime) {
		this.startLoading();
	}
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(Utils.PREF_PORTFOLIO_INCLUDE_FEE)) {
			this.startLoading();
		}
	}

	@Override
	public void onPortfolioChanged() {
		this.startLoading();
	}
}
