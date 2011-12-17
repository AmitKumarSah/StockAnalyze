package cz.tomas.StockAnalyze.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.UpdateScheduler;
import cz.tomas.StockAnalyze.StockList.StockListAdapter;
import cz.tomas.StockAnalyze.utils.Markets;
import cz.tomas.StockAnalyze.utils.NavUtils;

/**
 * Activity showing one list of wolrd indeces. 
 * Most functionality is just inherited from {@link StockListActivity}
 * @author tomas
 *
 */
public final class IndecesListActivity extends StockListActivity {
	
	/* (non-Javadoc)
	 * @see cz.tomas.StockAnalyze.activity.StockListActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/* (non-Javadoc)
	 * @see cz.tomas.StockAnalyze.activity.StockListActivity#createListAdapter()
	 */
	@Override
	protected StockListAdapter createListAdapter() {
		StockListAdapter adapter = new StockListAdapter(this, R.layout.item_stock_list, this.dataManager, null, true);
		return adapter;
	}

	@Override
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
