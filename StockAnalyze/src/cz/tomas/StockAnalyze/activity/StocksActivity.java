package cz.tomas.StockAnalyze.activity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.*;
import android.widget.Toast;
import com.viewpagerindicator.TitlePageIndicator;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.SearchResult;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.StockList.StocksPagerAdapter;
import cz.tomas.StockAnalyze.StockList.search.SearchStockItemTask;
import cz.tomas.StockAnalyze.UpdateScheduler;
import cz.tomas.StockAnalyze.fragments.ConfirmDialogFragment;
import cz.tomas.StockAnalyze.fragments.ProgressDialogFragment;
import cz.tomas.StockAnalyze.fragments.SearchStockDialogFragment;
import cz.tomas.StockAnalyze.ui.widgets.DragContainerView;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

import java.util.Collection;

/**
 * activity containing {@link ViewPager} with stock list for {@link Market} on each page
 * 
 * @author tomas
 *
 */
public final class StocksActivity extends AbstractStocksActivity implements OnPageChangeListener,
											AbstractStocksActivity.IDragSupportingActivity<StockItem> {

	private static final String TAG_CONFIRM = "confirm";

	private Market selectedMarket;

	private ViewPager pager;
	private DragContainerView dragContainer;
	private View container;
	private View actionPlusView;

	private SharedPreferences pref;
	private TitlePageIndicator titleIndicator;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		this.setContentView(R.layout.stocks);

		this.pref = this.getSharedPreferences(Utils.PREF_NAME, 0);
		this.container = this.findViewById(R.id.container);
		this.dragContainer = (DragContainerView) this.findViewById(R.id.dragContainer);
		this.pager = (ViewPager) this.findViewById(R.id.stocksViewPager);
		Collection<Market> markets = dataManager.getMarkets();
		this.pager.setAdapter(new StocksPagerAdapter(getSupportFragmentManager(), markets));

		titleIndicator = (TitlePageIndicator)findViewById(R.id.pagerTitles);
		titleIndicator.setOnPageChangeListener(this);

		if (markets != null) {
			onPrepareData(markets.toArray(new Market[markets.size()]));
		} else {
			this.showProgressDialog(R.string.loading, R.string.loadingMarkets, new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialogInterface) {
					finish();
				}
			});
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// this is for pre-honeycomb actionbar implementation,
		// where we need to access view from actionbar directly
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			this.actionPlusView = this.findViewById(R.id.menu_stock_add);
			this.actionPlusView.setVisibility(this.selectedMarket.getType() == Market.TYPE_SELECTIVE ?
												View.VISIBLE : View.GONE);
		}

	}

	@Override
	public void onStop() {
		super.onStop();
		this.pref.edit().putInt(Utils.PREF_STOCKS_POSITION, this.pager.getCurrentItem()).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.stock_list_menu, menu);
		MenuItem menuItemAdd = menu.findItem(R.id.menu_stock_add);
		menuItemAdd.setVisible(this.selectedMarket != null && this.selectedMarket.getType() == Market.TYPE_SELECTIVE);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_refresh:
	    	updateImmediately(this.selectedMarket);
	        return true;
	    case R.id.menu_stock_list_settings:
	    	NavUtils.goToSettings(this);
	        return true;
	    case R.id.menu_stock_add:
			searchForStock();
		    return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int position) {
		this.selectedMarket = ((StocksPagerAdapter) this.pager.getAdapter()).getMarketByPosition(position);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			this.invalidateOptionsMenu();
		} else {
			this.actionPlusView.setVisibility(this.selectedMarket.getType() == Market.TYPE_SELECTIVE ?
												View.VISIBLE : View.GONE);
		}
	}

	@Override
	protected void onPrepareData(Market[] markets) {
		this.dismissProgress();
		if (markets == null || markets.length == 0) {
			ConfirmDialogFragment dialog = ConfirmDialogFragment.newInstance(R.string.loadingMarketsFailed, new ConfirmDialogFragment.IConfirmListener() {
				@Override
				public void onConfirmed(ConfirmDialogFragment fragment) {
					finish();
				}
			});
			dialog.show(getSupportFragmentManager(), TAG_CONFIRM);
		} else {
			((StocksPagerAdapter) this.pager.getAdapter()).setMarkets(markets);
			int index = this.pref.getInt(Utils.PREF_STOCKS_POSITION, 0);
			if (index >= markets.length) {
				index = 0;
			}
			this.selectedMarket = markets[index];
			this.pager.setCurrentItem(index, false);
			this.titleIndicator.setViewPager(this.pager, index);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (dragContainer.isDragging()) {
			return dragContainer.dispatchTouchEvent(event);
		} else {
			return super.dispatchTouchEvent(event);
		}
	}

	@Override
	public void onStartDrag(StockItem data, View view, DragContainerView.IDragListener listener) {
		View root = (View) this.container.getParent();
		final int offsetX = this.pager.getLeft() + root.getLeft();
		final int offsetY = this.pager.getTop() + root.getTop();

		dragContainer.startDragging(view, view.getLeft() + view.getWidth() / 2, view.getTop() + view.getHeight() /2, offsetX, offsetY, data);
		dragContainer.setListener(listener);
	}

	private void searchForStock() {
		SearchStockDialogFragment fragment = SearchStockDialogFragment.newInstance(R.string.addStockItem, this.selectedMarket);
		fragment.setSearchListener(this.searchListener);
		fragment.show(getSupportFragmentManager(), "addStock");
	}

	private void addStock(SearchResult searchResult) {
		final ProgressDialogFragment fragment = ProgressDialogFragment.newInstance(R.string.addStockItem, R.string.loading);
		fragment.show(getSupportFragmentManager(), "add_progress");
		SearchStockItemTask task = new SearchStockItemTask(this, this.selectedMarket) {
			@Override
			protected void onPostExecute(StockItem stockItem) {
				fragment.dismiss();
				if (stockItem != null) {
					final String message = String.format("%s %s", stockItem.getName(), getText(R.string.addedStock).toString());
					Toast.makeText(StocksActivity.this, message, Toast.LENGTH_SHORT).show();
					// update data - this will cause fragment update too
					UpdateScheduler scheduler = (UpdateScheduler) getApplicationContext().getSystemService(Application.UPDATE_SCHEDULER_SERVICE);
					scheduler.updateImmediately(selectedMarket);
				} else {
					Toast.makeText(StocksActivity.this, R.string.addStockItemNotFound, Toast.LENGTH_SHORT).show();
				}
			}
		};
		task.execute(searchResult);
	}

	private final SearchStockDialogFragment.ISearchListener searchListener = new SearchStockDialogFragment.ISearchListener() {
		@Override
		public void onStockSelected(SearchResult searchResult) {
			addStock(searchResult);
		}
	};
}
