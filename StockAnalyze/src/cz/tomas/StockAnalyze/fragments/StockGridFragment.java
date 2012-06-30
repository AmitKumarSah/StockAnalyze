package cz.tomas.StockAnalyze.fragments;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.SearchResult;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.StockList.StockListAdapter;
import cz.tomas.StockAnalyze.StockList.search.SearchStockItemTask;
import cz.tomas.StockAnalyze.UpdateScheduler;
import cz.tomas.StockAnalyze.activity.AbstractStocksActivity;
import cz.tomas.StockAnalyze.fragments.StockFragmentHelper.IStockFragment;
import cz.tomas.StockAnalyze.ui.widgets.DragContainerView;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

public class StockGridFragment extends SherlockFragment implements IStockFragment, DragContainerView.IDragListener {
	
	public static final String ARA_INSECURE_INDICES = "includeIndeces";
	public static String ARG_MARKET = "market";

	private static final int DRAG_ID_PORTFOLIO = 1;
	private static final int DRAG_ID_REMOVE = 2;

	protected StockFragmentHelper helper;
	
	protected GridView grid;
	protected View progress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.helper = new StockFragmentHelper(this, getArguments());
		this.setHasOptionsMenu(true);
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
		
		if (! (getActivity() instanceof AbstractStocksActivity.IDragSupportingActivity)) {
			throw new IllegalStateException("fragment's activity must implement IDragSupportingActivity");
		}
		final StockListAdapter adapter = new StockListAdapter(getActivity(), R.layout.item_stock_grid);
		this.helper.setAdapter(adapter);

		this.grid.setAdapter(adapter);
		this.grid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View arg1, int position, long arg3) {
				StockItem stock = adapter.getItem(position);
				NavUtils.goToStockDetail(stock, adapter.getDayData(stock), getActivity());
			}
		});
		this.grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
				StockItem item = adapter.getItem(position);
				startDrag(view, item);
				return true;
			}
		});
		LoaderManager.enableDebugLogging(Utils.DEBUG);
		getLoaderManager().initLoader(0, null, this.helper);
		this.progress.setVisibility(View.VISIBLE);
	}

	@SuppressWarnings("unchecked")
	private void startDrag(View view, StockItem item) {
		int color1, color1Active;
		color1 = getResources().getColor(R.color.drag_container_action1);
		color1Active = getResources().getColor(R.color.drag_container_action1_active);
		DragContainerView.DragTarget portfolioTarget = new DragContainerView.DragTarget(DRAG_ID_PORTFOLIO, color1, color1Active, getString(R.string.homeMyPortfolio));
		if (this.helper.getMarket().getType() == Market.TYPE_SELECTIVE) {
			// add remove target
			int color2, color2Active;
			color2 = getResources().getColor(R.color.drag_container_action2);
			color2Active = getResources().getColor(R.color.drag_container_action2_active);
			DragContainerView.DragTarget removeTarget = new DragContainerView.DragTarget(DRAG_ID_REMOVE, color2, color2Active, getString(R.string.stockHide));
			((AbstractStocksActivity.IDragSupportingActivity) getActivity()).onStartDrag(item, view, this, portfolioTarget, removeTarget);
		} else {
			((AbstractStocksActivity.IDragSupportingActivity) getActivity()).onStartDrag(item, view, this, portfolioTarget);
		}

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (this.helper.getMarket() != null && this.helper.getMarket().getType() == Market.TYPE_SELECTIVE) {
			menu.add(0, R.id.menu_stock_add, Menu.NONE, R.string.addStockItem);
			final MenuItem menuItem = menu.findItem(R.id.menu_stock_add);
			menuItem.setIcon(R.drawable.ic_action_plus);
			menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		super.onCreateOptionsMenu(menu, inflater);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_stock_add) {
			searchForStock();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onLoadFinished() {
		if (this.getActivity() != null) {
			this.progress.setVisibility(View.GONE);
		}
	}

	@Override
	public void onDragComplete(Object data, DragContainerView.DragTarget target) {
		if (target != null && target.id == DRAG_ID_PORTFOLIO) {
			NavUtils.goToAddToPortfolio(getActivity(), (StockItem) data, null);
		}
	}

	private void searchForStock() {
		SearchStockDialogFragment fragment = SearchStockDialogFragment.newInstance(R.string.addStockItem, this.helper.getMarket());
		fragment.setSearchListener(this.searchListener);
		fragment.show(getFragmentManager(), "addStock");
	}

	private void addStock(SearchResult searchResult) {
		final ProgressDialogFragment fragment = ProgressDialogFragment.newInstance(R.string.addStockItem, R.string.loading);
		fragment.show(getFragmentManager(), "add_progress");

		final Market market = this.helper.getMarket();
		SearchStockItemTask task = new MySearchStockItemTask(market, fragment);
		task.execute(searchResult);
	}

	private final SearchStockDialogFragment.ISearchListener searchListener = new SearchStockDialogFragment.ISearchListener() {
		@Override
		public void onStockSelected(SearchResult searchResult) {
			addStock(searchResult);
		}
	};

	private class MySearchStockItemTask extends SearchStockItemTask {
		private final Market market;
		private final ProgressDialogFragment fragment;

		public MySearchStockItemTask(Market market, ProgressDialogFragment fragment) {
			super(StockGridFragment.this.getActivity(), market);
			this.market = market;
			this.fragment = fragment;
		}

		@Override
		protected void onPostExecute(StockItem stockItem) {
			fragment.dismiss();
			if (stockItem != null) {
				final String message = String.format("%s %s", stockItem.getName(), getText(R.string.addedStock).toString());
				Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
				// update data - this will cause fragment update too
				UpdateScheduler scheduler = (UpdateScheduler) getActivity().getApplicationContext().getSystemService(Application.UPDATE_SCHEDULER_SERVICE);
				scheduler.updateImmediately(market);
			} else {
				Toast.makeText(getActivity(), R.string.addStockItemNotFound, Toast.LENGTH_SHORT).show();
			}
		}
	}
}
