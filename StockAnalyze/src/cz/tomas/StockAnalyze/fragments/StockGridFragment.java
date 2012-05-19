package cz.tomas.StockAnalyze.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.StockList.StockListAdapter;
import cz.tomas.StockAnalyze.activity.AbstractStocksActivity;
import cz.tomas.StockAnalyze.fragments.StockFragmentHelper.IStockFragment;
import cz.tomas.StockAnalyze.ui.widgets.DragContainerView;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

public class StockGridFragment extends Fragment implements IStockFragment, DragContainerView.IDragListener {
	
	public static final String ARA_INSECURE_INDICES = "includeIndeces";
	public static String ARG_MARKET = "market";

	private static final int DRAG_ID_PORTFOLIO = 1;
	private static final int DRAG_ID_REMOVE = 2;

	protected StockFragmentHelper helper;
	
	protected GridView grid;
	protected View progress;

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
		this.helper = new StockFragmentHelper(this, getArguments(), adapter);
		
		//this.registerForContextMenu(this.grid);
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

	@Override
	public void onDragComplete(Object data, DragContainerView.DragTarget target) {
		if (target != null && target.id == DRAG_ID_PORTFOLIO) {
			NavUtils.goToAddToPortfolio(getActivity(), (StockItem) data, null);
		}
	}
}
