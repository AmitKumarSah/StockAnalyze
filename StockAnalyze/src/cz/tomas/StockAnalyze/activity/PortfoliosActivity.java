package cz.tomas.StockAnalyze.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Portfolio.PortfolioMarketsLoader;
import cz.tomas.StockAnalyze.Portfolio.PortfolioPagerAdapter;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.fragments.ConfirmDialogFragment;
import cz.tomas.StockAnalyze.ui.widgets.PickStockDialog;
import cz.tomas.StockAnalyze.ui.widgets.PickStockDialog.IStockDialogListener;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

import java.util.Collection;

/**
 * activity vith {@link ViewPager} with portfolio for each currency as a page
 * @author tomas
 *
 */
public final class PortfoliosActivity extends AbstractStocksActivity implements LoaderManager.LoaderCallbacks<Collection<Market>>, OnPageChangeListener {

	public static final int DIALOG_PROGRESS = 1000;
	public static final int DIALOG_ADD_NEW = DIALOG_PROGRESS + 1;
	
	public static final String EXTRA_STOCK_ITEM = "portfolioStockItem";

	private PortfolioPagerAdapter pagerAdapter;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		this.setContentView(R.layout.portfolios);

		ViewPager pager = (ViewPager) this.findViewById(R.id.portfoliosViewPager);
		this.pagerAdapter = new PortfolioPagerAdapter(getSupportFragmentManager(), null);
		pager.setAdapter(pagerAdapter);

		if (dataManager.isMarketCollectionAvailable()) {
			onPrepareData(dataManager.getMarkets());
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
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_PROGRESS:
			final CharSequence text = getText(R.string.working);
			final ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage(text);
			return dialog;
		case DIALOG_ADD_NEW:
			final PickStockDialog pickStockDialog = new PickStockDialog(this, false);
			pickStockDialog.setListener(new IStockDialogListener() {
				
				@Override
				public void onStockSelected(StockItem item) {
					if (item != null) {
						pickStockDialog.dismiss();
						NavUtils.goToAddToPortfolio(PortfoliosActivity.this, item, null);
					}
				}
			});
			
			return pickStockDialog;
		default:
			break;
		}
		return super.onCreateDialog(id);
	}

	/**
	 * create activity's main menu
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.portfolio_menu, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		this.setIntent(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.menu_refresh:
	        return super.onOptionsItemSelected(item);
	    case R.id.menu_portfolio_settings:
	    	NavUtils.goToSettings(this);
	        return true;
	    case R.id.menu_portfolio_add:
	    	this.showDialog(DIALOG_ADD_NEW);
		    this.getSupportLoaderManager().restartLoader(0, null, this);
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
	}
	
	@Override
	protected void onNavigateUp() {
		NavUtils.goUp(this, HomeActivity.class);
	}

	protected void onPrepareData(Collection<Market> markets) {
		if (Utils.DEBUG) Log.d(Utils.LOG_TAG, "preparing data for portfolio from " + markets);
		this.dismissProgress();
		if (markets == null || markets.size() == 0) {
			ConfirmDialogFragment dialog = ConfirmDialogFragment.newInstance(R.string.loadingMarketsFailed, new ConfirmDialogFragment.IConfirmListener() {
				@Override
				public void onConfirmed(ConfirmDialogFragment fragment) {
					finish();
				}
			});
			dialog.show(getSupportFragmentManager(), TAG_CONFIRM);
		} else {
			getSupportLoaderManager().initLoader(0, null, this);
		}
	}

	@Override
	public Loader<Collection<Market>> onCreateLoader(int id, Bundle args) {
		return new PortfolioMarketsLoader(this);
	}

	@Override
	public void onLoadFinished(Loader<Collection<Market>> loader, Collection<Market> data) {
		this.pagerAdapter.setMarkets(data);
	}

	@Override
	public void onLoaderReset(Loader<Collection<Market>> loader) {
		this.pagerAdapter.setMarkets(null);
	}
}
