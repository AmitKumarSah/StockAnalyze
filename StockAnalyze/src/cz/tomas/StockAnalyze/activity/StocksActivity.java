package cz.tomas.StockAnalyze.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.jakewharton.android.viewpagerindicator.TitlePageIndicator;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.StockList.StocksPagerAdapter;
import cz.tomas.StockAnalyze.UpdateScheduler;
import cz.tomas.StockAnalyze.utils.NavUtils;

import java.util.Collection;

/**
 * activity containing {@link ViewPager} with stock list for {@link Market} on each page
 * 
 * @author tomas
 *
 */
public final class StocksActivity extends AbstractStocksActivity implements OnPageChangeListener {
	
	private Market selectedMarket;
	
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
		Collection<Market> markets = DataManager.getInstance(this).getMarkets();
		this.pager.setAdapter(new StocksPagerAdapter(getSupportFragmentManager(), markets));
		if (markets != null) {
			this.selectedMarket = markets.iterator().next();
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
	    	updateImmediatly(this.selectedMarket);
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
}
