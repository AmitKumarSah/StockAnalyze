package cz.tomas.StockAnalyze.activity;

import java.util.List;

import com.jakewharton.android.viewpagerindicator.TitlePageIndicator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.UpdateScheduler;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.StockList.StocksPagerAdapter;
import cz.tomas.StockAnalyze.activity.base.BaseFragmentActivity;
import cz.tomas.StockAnalyze.ui.widgets.ActionBar;
import cz.tomas.StockAnalyze.ui.widgets.ActionBar.IActionBarListener;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * activity containing {@link ViewPager} with stock list for market in each page
 * 
 * @author tomas
 *
 */
public final class StocksActivity extends BaseFragmentActivity implements IActionBarListener, OnPageChangeListener {
	
	static final int UPDATE_DLG_SUCCES = 0;
	static final int UPDATE_DLG_FAIL = 1;
	static final int NO_INTERNET = 2;
	
	private View refreshButton;
	private Animation refreshAnim;
	private Market selectedMarket;
	
	private ViewPager pager;
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		this.setContentView(R.layout.stocks);
		try {
			this.refreshButton = findViewById(R.id.actionRefreshButton);
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to find refresh button", e);
		}
		ActionBar bar = (ActionBar) findViewById(R.id.stockListActionBar);
		if (bar != null) {
			bar.setActionBarListener(this);
		}
		this.pager = (ViewPager) this.findViewById(R.id.stocksViewPager);
		List<Market> markets = DataManager.getInstance(this).getMarkets();
		this.pager.setAdapter(new StocksPagerAdapter(getSupportFragmentManager(), markets));
		if (markets != null) {
			this.selectedMarket = markets.get(0);
		}
		//Bind the title indicator to the adapter
		TitlePageIndicator titleIndicator = (TitlePageIndicator)findViewById(R.id.pagerTitles);
		titleIndicator.setViewPager(pager);
		titleIndicator.setOnPageChangeListener(this);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dlg = null;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setPositiveButton("OK", new Dialog.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dlg, int arg1) {
				dlg.dismiss();
			}
		});
		
		switch (id)
		{
		case UPDATE_DLG_SUCCES:
			builder.setMessage(R.string.update_succes);
			dlg = builder.create();
			break;
		case UPDATE_DLG_FAIL:
			builder.setMessage(R.string.update_fail);
			dlg = builder.create();
			break;
		case NO_INTERNET:
			builder.setMessage(R.string.NoInternet);
			dlg = builder.create();
			break;
		}
		
		return dlg;
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
	    case R.id.menu_stock_list_refresh:
	    	updateImmediatly();
	        return true;
	    case R.id.menu_stock_list_settings:
	    	NavUtils.goToSettings(this);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public void onAction(int viewId) {
		if (viewId == R.id.actionRefreshButton) {
			updateImmediatly();
		}
	}

	/**
	 * call {@link UpdateScheduler} to update data
	 */
	protected void updateImmediatly() {
		UpdateScheduler scheduler = 
			(UpdateScheduler) this.getApplicationContext().getSystemService(Application.UPDATE_SCHEDULER_SERVICE);
		if (this.selectedMarket == null) {
			scheduler.updateImmediatly();
		} else {
			scheduler.updateImmediatly(this.selectedMarket);
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
		Market market = ((StocksPagerAdapter) this.pager.getAdapter()).getMarketByPosition(position);
		this.selectedMarket = market;
	}
	
	
}
