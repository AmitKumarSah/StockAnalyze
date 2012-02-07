package cz.tomas.StockAnalyze.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListAdapter;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.StockList.StockListAdapter;
import cz.tomas.StockAnalyze.StockList.StocksLoader;
import cz.tomas.StockAnalyze.utils.NavUtils;

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
	private final StockListAdapter adapter;
	
	StockFragmentHelper(IStockFragment fragment, Bundle bundle, StockListAdapter adapter) {
		this.fragment = fragment;
		this.adapter = adapter;

		this.market = (Market) bundle.get(StockGridFragment.ARG_MARKET);
	}
	
	boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		// we need to have adapter from this listview
		ListAdapter listAdapter = ((AbsListView) info.targetView.getParent()).getAdapter();
		if (info.position >= listAdapter.getCount()) {
			return false;
		}
		StockItem stockItem = (StockItem) listAdapter.getItem(info.position);
		if (stockItem == null) {
			return false;
		}
		DayData data = this.adapter.getDayData(stockItem);
		
		switch (item.getItemId()) {
			case R.id.stock_item_add_to_portfolio:
				NavUtils.goToAddToPortfolio(fragment.getActivity(), stockItem, data);
				return true;
			case R.id.stock_item_favourite:
				// TODO mark as favourite
				return true;
			case R.id.stock_item_view:
				NavUtils.goToStockDetail(stockItem, this.adapter.getDayData(stockItem), fragment.getActivity());
				return true;
			default:
				return false;
		}
	}
	
	void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		MenuInflater inflater = this.fragment.getActivity().getMenuInflater();
		inflater.inflate(R.menu.stock_item_context_menu, menu);
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
