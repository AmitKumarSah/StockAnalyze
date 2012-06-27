package cz.tomas.StockAnalyze.activity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.StockList.StocksPagerAdapter;
import cz.tomas.StockAnalyze.fragments.ConfirmDialogFragment;
import cz.tomas.StockAnalyze.fragments.SetMarketsDialogFragment;
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
public final class StocksActivity extends AbstractStocksActivity implements
											AbstractStocksActivity.IDragSupportingActivity<StockItem>,
											SetMarketsDialogFragment.IMarketsActivity {

	private static final String TAG_CONFIRM = "confirm";

	private Market selectedMarket;

	private ViewPager pager;
	private DragContainerView dragContainer;
	private View container;

	private SharedPreferences pref;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		this.setContentView(R.layout.stocks);

		this.pref = this.getSharedPreferences(Utils.PREF_NAME, 0);
		this.container = this.findViewById(R.id.container);
		this.dragContainer = (DragContainerView) this.findViewById(R.id.dragContainer);
		this.pager = (ViewPager) this.findViewById(R.id.stocksViewPager);
		this.pager.setPageMargin((int) getResources().getDimension(R.dimen.padding_quantum_half));

		loadMarkets();
	}

	private void loadMarkets() {
		Collection<Market> markets = dataManager.getMarkets();
		if (this.pager.getAdapter() == null) {
			this.pager.setAdapter(new StocksPagerAdapter(getSupportFragmentManager()));
		}

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
	public void onStop() {
		super.onStop();
		this.pref.edit().putInt(Utils.PREF_STOCKS_POSITION, this.pager.getCurrentItem()).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.stock_list_menu, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.menu_refresh:
	    	updateImmediately(this.selectedMarket);
	        return true;
	    case R.id.menu_stock_list_settings:
	    	NavUtils.goToSettings(this);
	        return true;
	    case R.id.menu_pages:
		    SetMarketsDialogFragment fragment = new SetMarketsDialogFragment();
		    fragment.show(getSupportFragmentManager(), "setMarkets");
			return true;
	    default:
	        return super.onOptionsItemSelected(item);
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
	public void onStartDrag(StockItem data, View view, DragContainerView.IDragListener listener, DragContainerView.DragTarget... targets) {
		View root = (View) this.container.getParent();
		final int offsetX = this.pager.getLeft() + root.getLeft();
		final int offsetY = this.pager.getTop() + root.getTop();

		dragContainer.startDragging(view, view.getLeft() + view.getWidth() / 2, view.getTop() + view.getHeight() /2, offsetX, offsetY, data, targets);
		dragContainer.setListener(listener);
	}

	@Override
	public void onUpdateMarkets() {
		loadMarkets();
	}
}
