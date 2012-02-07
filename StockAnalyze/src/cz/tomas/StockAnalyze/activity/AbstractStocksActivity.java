package cz.tomas.StockAnalyze.activity;

import cz.tomas.StockAnalyze.UpdateScheduler;
import cz.tomas.StockAnalyze.Data.Interfaces.IUpdateSchedulerListener;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.activity.base.BaseFragmentActivity;
import cz.tomas.StockAnalyze.utils.NavUtils;

public class AbstractStocksActivity extends BaseFragmentActivity {

	protected UpdateScheduler sheduler;
	
	private final IUpdateSchedulerListener updateListener = new IUpdateSchedulerListener() {
			
			@Override
			public void onUpdateFinished(boolean success) {
				getActionBarHelper().setRefreshActionItemState(false);
			}
			
			@Override
			public void onUpdateBegin(Market... markets) {
				getActionBarHelper().setRefreshActionItemState(true);
			}
		};

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

	protected void updateImmediatly(Market market) {
		this.sheduler.updateImmediately(market);
	}

	@Override
	protected void onNavigateUp() {
		NavUtils.goUp(this, HomeActivity.class);
	}
}