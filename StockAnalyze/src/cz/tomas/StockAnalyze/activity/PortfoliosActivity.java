package cz.tomas.StockAnalyze.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TitlePageIndicator;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Portfolio.PortfolioPagerAdapter;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.fragments.ConfirmDialogFragment;
import cz.tomas.StockAnalyze.ui.widgets.PickStockDialog;
import cz.tomas.StockAnalyze.ui.widgets.PickStockDialog.IStockDialogListener;
import cz.tomas.StockAnalyze.utils.NavUtils;

import java.util.Collection;

/**
 * activity vith {@link ViewPager} with portfolio for each currency as a page
 * @author tomas
 *
 */
public final class PortfoliosActivity extends AbstractStocksActivity implements OnPageChangeListener {

	public static final int DIALOG_PROGRESS = 1000;
	public static final int DIALOG_ADD_NEW = DIALOG_PROGRESS + 1;
	
	public static final String EXTRA_STOCK_ITEM = "portfolioStockItem";
	
	private ViewPager pager;
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		this.setContentView(R.layout.portfolios);
		this.setAsTopLevelActivity(NAVIGATION_PORTFOLIO);

		this.pager = (ViewPager) this.findViewById(R.id.portfoliosViewPager);
		Collection<Market> markets = dataManager.getMarkets();

		if (markets != null) {
			onPrepareData(markets);
		} else {
			this.showProgressDialog(R.string.loading, R.string.loadingMarkets, new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialogInterface) {
					finish();
				}
			});
		}
		
		//Bind the title indicator to the adapter
		TitlePageIndicator titleIndicator = (TitlePageIndicator)findViewById(R.id.portfoliosPagerTitles);
		titleIndicator.setViewPager(pager);
		titleIndicator.setOnPageChangeListener(this);
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
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_refresh:
	    	//this.adapter.refresh();
	        return super.onOptionsItemSelected(item);
	    case R.id.menu_portfolio_settings:
	    	NavUtils.goToSettings(this);
	        return true;
	    case R.id.menu_portfolio_add:
	    	this.showDialog(DIALOG_ADD_NEW);
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
			this.pager.setAdapter(new PortfolioPagerAdapter(getSupportFragmentManager(), markets));
			this.pager.setCurrentItem(0);
		}
	}
}
