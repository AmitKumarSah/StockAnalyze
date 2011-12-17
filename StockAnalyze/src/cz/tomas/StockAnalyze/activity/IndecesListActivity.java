package cz.tomas.StockAnalyze.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.UpdateScheduler;
import cz.tomas.StockAnalyze.activity.base.BaseFragmentActivity;
import cz.tomas.StockAnalyze.fragments.StockGridFragment;
import cz.tomas.StockAnalyze.utils.Markets;
import cz.tomas.StockAnalyze.utils.NavUtils;

/**
 * Activity showing a fragment with list of world indices. 
 * @author tomas
 *
 */
public final class IndecesListActivity extends BaseFragmentActivity {
	
	private static final int CONTAINER_ID = 1000;
	private static final String FRAGMENT_TAG = "stocklist";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		FrameLayout container = new FrameLayout(this);
		container.setId(CONTAINER_ID);
		this.setContentView(container);
		
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction tran =  manager.beginTransaction();
		Fragment fragment = manager.findFragmentByTag(FRAGMENT_TAG);
		if (fragment != null) {
			tran.attach(fragment);
		} else {
			fragment = new StockGridFragment();
			Bundle args = new Bundle();
			args.putSerializable(StockGridFragment.ARG_MARKET, Markets.GLOBAL);
			fragment.setArguments(args);
			tran.add(container.getId(), fragment, FRAGMENT_TAG);
		}
		tran.commit();
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
	
	protected void updateImmediatly() {
		UpdateScheduler scheduler = 
			(UpdateScheduler) this.getApplicationContext().getSystemService(Application.UPDATE_SCHEDULER_SERVICE);
		scheduler.updateImmediatly(Markets.GLOBAL);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.indeces_menu, menu);
	    return true;
	}

	@Override
	protected void onNavigateUp() {
		NavUtils.goUp(this, HomeActivity.class);
	}
}
