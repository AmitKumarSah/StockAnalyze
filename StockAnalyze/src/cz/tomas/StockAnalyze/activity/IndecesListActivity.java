package cz.tomas.StockAnalyze.activity;

import android.os.Bundle;
import android.widget.TextView;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.UpdateScheduler;
import cz.tomas.StockAnalyze.StockList.StockListAdapter;
import cz.tomas.StockAnalyze.utils.Markets;

public final class IndecesListActivity extends StockListActivity {
	
	/* (non-Javadoc)
	 * @see cz.tomas.StockAnalyze.activity.StockListActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		TextView title = (TextView) this.findViewById(R.id.actionTitle);
//		title.setText(R.string.homeIndeces);
	}

	/* (non-Javadoc)
	 * @see cz.tomas.StockAnalyze.activity.StockListActivity#createListAdapter()
	 */
	@Override
	protected StockListAdapter createListAdapter() {
		StockListAdapter adapter = new StockListAdapter(this, R.layout.stock_list, this.dataManager, null, true);
		return adapter;
	}

	@Override
	protected void updateImmediatly() {
		UpdateScheduler scheduler = 
			(UpdateScheduler) this.getApplicationContext().getSystemService(Application.UPDATE_SCHEDULER_SERVICE);
		scheduler.updateImmediatly(Markets.GLOBAL);
	}
}
