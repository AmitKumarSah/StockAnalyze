package cz.tomas.StockAnalyze.activity;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.jakewharton.android.viewpagerindicator.TitlePageIndicator;

import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.UpdateScheduler;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Interfaces.IUpdateSchedulerListener;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.StockList.StocksPagerAdapter;
import cz.tomas.StockAnalyze.activity.base.BaseFragmentActivity;
import cz.tomas.StockAnalyze.utils.NavUtils;

/**
 * activity containing {@link ViewPager} with stock list for {@link Market} on each page
 * 
 * @author tomas
 *
 */
public final class StocksActivity extends BaseFragmentActivity implements OnPageChangeListener {
	
	static final int UPDATE_DLG_SUCCES = 0;
	static final int UPDATE_DLG_FAIL = 1;
	static final int NO_INTERNET = 2;
	
	private Market selectedMarket;
	private UpdateScheduler sheduler;
	
	private ViewPager pager;
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		this.setContentView(R.layout.stocks);
		this.sheduler = (UpdateScheduler) this.getApplicationContext().getSystemService(Application.UPDATE_SCHEDULER_SERVICE);

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

        //ViewServer.get(this).addWindow(this);
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
	    case R.id.menu_refresh:
	    	updateImmediatly();
	        return true;
	    case R.id.menu_stock_list_settings:
	    	NavUtils.goToSettings(this);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	private final IUpdateSchedulerListener updateListener = new IUpdateSchedulerListener() {
		
		@Override
		public void onUpdateFinished(boolean succes) {
			getActionBarHelper().setRefreshActionItemState(false);
		}
		
		@Override
		public void onUpdateBegin(Market... markets) {
			getActionBarHelper().setRefreshActionItemState(true);
		}
	};
	
    @Override
	protected void onPause() {
		super.onPause();
		//ViewServer.get(this).removeWindow(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		//ViewServer.get(this).setFocusedWindow(this);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		this.sheduler.addListener(this.updateListener);
	}

	@Override
	public void onStop() {
		super.onStop();
		this.sheduler.removeListener(this.updateListener);
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
	
	@Override
	protected void onNavigateUp() {
		NavUtils.goUp(this, HomeActivity.class);
	}	
}
