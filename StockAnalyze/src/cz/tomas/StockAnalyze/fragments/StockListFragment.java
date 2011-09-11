package cz.tomas.StockAnalyze.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.StockList.StockListAdapter;
import cz.tomas.StockAnalyze.utils.NavUtils;

/**
 * Fragment with list of stocks and their prices.
 * It needs to have {@link Market} in arguments - see setArguments(Bundle b)
 * @author tomas
 *
 */
public class StockListFragment extends ListFragment {

	public static String ARG_MARKET = "market";
	
	protected DataManager dataManager;

	private StockListAdapter adapter;
	
	private Market market;
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.dataManager = (DataManager) getActivity().getApplicationContext().getSystemService(Application.DATA_MANAGER_SERVICE);
		
		//this.setContentView(R.layout.stock_list);
		this.getListView().setTextFilterEnabled(true);
		this.registerForContextMenu(this.getListView());

		this.getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				StockItem stock = (StockItem) getListView().getItemAtPosition(position);
				NavUtils.goToStockDetail(stock, adapter.getDayData(stock), getActivity());
			}

		});

		this.market = (Market) getArguments().get(ARG_MARKET);
		this.adapter = createListAdapter();
		this.setListAdapter(adapter);
		this.setEmptyText(getString(R.string.loading));
	}
	
	/**
	 * create adapter instance
	 */
	protected StockListAdapter createListAdapter() {
		StockListAdapter adapter = new StockListAdapter(getActivity(), R.layout.stock_list, 
				this.dataManager, this.market, false);
		adapter.showIcons(false);
		return adapter;
	}

	@Override
	public void onResume() {
		super.onResume();
		adapter.attachToData();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {
		super.onPause();
		this.adapter.detachFromData();
	}
	
	/** 
	 * stock context menu for stock item
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		// we need to have adapter from this listview
		ListAdapter listAdapter = ((ListView) info.targetView.getParent()).getAdapter();
		if (info.position >= listAdapter.getCount()) {
			return false;
		}
		StockItem stockItem = (StockItem) listAdapter.getItem(info.position);
		if (stockItem == null) {
			return false;
		}
		DayData data = adapter.getDayData(stockItem);
		
		switch (item.getItemId()) {
			case R.id.stock_item_add_to_portfolio:
				NavUtils.goToAddToPortfolio(getActivity(), stockItem, data);
				return true;
			case R.id.stock_item_favourite:
				// TODO mark as favourite
				return true;
			case R.id.stock_item_view:
				NavUtils.goToStockDetail(stockItem, adapter.getDayData(stockItem), getActivity());
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	/** 
	 * context menu for all stock items in list view
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.stock_item_context_menu, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}
}
