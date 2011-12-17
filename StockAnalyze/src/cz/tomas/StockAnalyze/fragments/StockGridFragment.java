package cz.tomas.StockAnalyze.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListAdapter;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Interfaces.IListAdapterListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.StockList.StockListAdapter;
import cz.tomas.StockAnalyze.utils.NavUtils;

public final class StockGridFragment extends Fragment implements IListAdapterListener<Object> {
	
	public static String ARG_MARKET = "market";
	
	protected DataManager dataManager;

	private StockListAdapter adapter;
	
	private Market market;
	
	private GridView grid;
	private View progress;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.stock_grid, container, false);
		this.grid = (GridView) v.findViewById(R.id.gridview);
		this.progress = v.findViewById(R.id.progress);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.dataManager = (DataManager) getActivity().getApplicationContext().getSystemService(Application.DATA_MANAGER_SERVICE);
		
		this.market = (Market) getArguments().get(ARG_MARKET);
		this.adapter = new StockListAdapter(getActivity(), R.layout.item_stock_grid, this.dataManager, this.market, false);
		this.adapter.addListAdapterListener(this);
		
		this.registerForContextMenu(this.grid);
		this.grid.setAdapter(this.adapter);
		this.grid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View arg1, int position, long arg3) {
				StockItem stock = (StockItem) adapter.getItem(position);
				NavUtils.goToStockDetail(stock, adapter.getDayData(stock), getActivity());
			}
		});
	}


	/**
	 * create adapter instance
	 */
	protected StockListAdapter createListAdapter() {
		StockListAdapter adapter = new StockListAdapter(getActivity(), R.layout.item_stock_list, 
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
		ListAdapter listAdapter = ((AbsListView) info.targetView.getParent()).getAdapter();
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

	@Override
	public void onListLoading() {
		if (this.getActivity() != null) {
			this.progress.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onListLoaded(Object data) {
		if (this.getActivity() != null) {
			this.progress.setVisibility(View.GONE);
		}
	}
}
