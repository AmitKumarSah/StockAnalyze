package cz.tomas.StockAnalyze.activity;

import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.jakewharton.android.viewpagerindicator.TitlePageIndicator;

import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Portfolio.PortfolioPagerAdapter;
import cz.tomas.StockAnalyze.activity.base.BaseFragmentActivity;
import cz.tomas.StockAnalyze.ui.widgets.PickStockDialog;
import cz.tomas.StockAnalyze.ui.widgets.PickStockDialog.IStockDialogListener;
import cz.tomas.StockAnalyze.utils.NavUtils;

public final class PortfoliosActivity extends BaseFragmentActivity implements OnPageChangeListener {

	public static final int DIALOG_PROGRESS = 1000;
	public static final int DIALOG_ADD_NEW = DIALOG_PROGRESS + 1;
	
	private ViewPager pager;
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		this.setContentView(R.layout.portfolios);
		
		this.pager = (ViewPager) this.findViewById(R.id.portfoliosViewPager);
		List<Market> markets = DataManager.getInstance(this).getMarkets();
		this.pager.setAdapter(new PortfolioPagerAdapter(getSupportFragmentManager(), markets));
		
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
	    MenuInflater inflater = getMenuInflater();
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
	    case R.id.menu_portfolio_refresh:
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
}
