package cz.tomas.StockAnalyze.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.*;
import com.viewpagerindicator.TitlePageIndicator;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.StockList.StocksPagerAdapter;
import cz.tomas.StockAnalyze.fragments.ConfirmDialogFragment;
import cz.tomas.StockAnalyze.ui.widgets.DragContainerView;
import cz.tomas.StockAnalyze.utils.NavUtils;

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

	/* (non-Javadoc)
		 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
		 */
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		this.setContentView(R.layout.stocks);

		this.container = this.findViewById(R.id.container);
		this.dragContainer = (DragContainerView) this.findViewById(R.id.dragContainer);
		this.pager = (ViewPager) this.findViewById(R.id.stocksViewPager);
		Collection<Market> markets = dataManager.getMarkets();
		this.pager.setAdapter(new StocksPagerAdapter(getSupportFragmentManager(), markets));

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

		//Bind the title indicator to the adapter
		TitlePageIndicator titleIndicator = (TitlePageIndicator)findViewById(R.id.pagerTitles);
		titleIndicator.setViewPager(pager);
		titleIndicator.setOnPageChangeListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.stock_list_menu, menu);
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
			this.selectedMarket = markets[0];
			this.pager.setCurrentItem(0);
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
}
