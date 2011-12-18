package cz.tomas.StockAnalyze.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.StockList.StockListAdapter;
import cz.tomas.StockAnalyze.fragments.StockFragmentHelper.IStockFragment;
import cz.tomas.StockAnalyze.utils.NavUtils;

public final class StockGridFragment extends Fragment implements IStockFragment {
	
	public static String ARG_MARKET = "market";

	private StockFragmentHelper helper;
	
	private GridView grid;
	private View progress;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setRetainInstance(true);
		super.onCreate(savedInstanceState);
	}

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
		
		final Market market = (Market) getArguments().get(ARG_MARKET);
		final StockListAdapter adapter = new StockListAdapter(getActivity(), R.layout.item_stock_grid);
		this.helper = new StockFragmentHelper(this, market, adapter);
		
		this.registerForContextMenu(this.grid);
		this.grid.setAdapter(adapter);
		this.grid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View arg1, int position, long arg3) {
				StockItem stock = (StockItem) adapter.getItem(position);
				NavUtils.goToStockDetail(stock, adapter.getDayData(stock), getActivity());
			}
		});
		getLoaderManager().initLoader(0, null, this.helper);
		this.progress.setVisibility(View.VISIBLE);
	}
	

	/** 
	 * stock context menu for stock item
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return this.helper.onContextItemSelected(item);
	}

	/** 
	 * context menu for all stock items in list view
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		this.helper.onCreateContextMenu(menu, v, menuInfo);
		super.onCreateContextMenu(menu, v, menuInfo);
	}


	@Override
	public void onLoadFinished() {
		if (this.getActivity() != null) {
			this.progress.setVisibility(View.GONE);
		}
	}
}
