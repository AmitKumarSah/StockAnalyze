package cz.tomas.StockAnalyze.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.StockList.StockListAdapter;
import cz.tomas.StockAnalyze.StockList.StocksLoader;

import java.util.Map;

/**
 * Helper class containing common functionality from stock fragments,
 * for now {@link StockListFragment} and {@link StockGridFragment}
 * @author tomas
 *
 */
final class StockFragmentHelper implements LoaderCallbacks<Map<StockItem, DayData>> {

	interface IStockFragment {
		Activity getActivity();
		void onLoadFinished();
	}
	
	private final IStockFragment fragment;
	private final Market market;

	private StockListAdapter adapter;

	StockFragmentHelper(IStockFragment fragment, Bundle bundle) {
		this(fragment, bundle, null);
	}

	StockFragmentHelper(IStockFragment fragment, Bundle bundle, StockListAdapter adapter) {
		this.fragment = fragment;
		this.adapter = adapter;

		this.market = (Market) bundle.get(StockGridFragment.ARG_MARKET);
	}

	public Market getMarket() {
		return market;
	}

	public void setAdapter(StockListAdapter adapter) {
		this.adapter = adapter;
	}

	@Override
	public Loader<Map<StockItem, DayData>> onCreateLoader(int id, Bundle args) {
		return new StocksLoader(this.fragment.getActivity(), this.market);
	}

	@Override
	public void onLoadFinished(Loader<Map<StockItem, DayData>> loader,
			Map<StockItem, DayData> data) {
		this.adapter.setData(data);
		this.fragment.onLoadFinished();
	}


	@Override
	public void onLoaderReset(Loader<Map<StockItem, DayData>> loader) {
		if (adapter != null) {
			adapter.setData(null);
		}
	}
}
