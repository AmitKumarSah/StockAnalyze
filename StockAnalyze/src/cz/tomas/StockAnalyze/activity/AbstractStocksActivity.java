package cz.tomas.StockAnalyze.activity;

import android.os.Bundle;
import android.view.View;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Interfaces.IMarketListener;
import cz.tomas.StockAnalyze.Data.Interfaces.IUpdateSchedulerListener;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.UpdateScheduler;
import cz.tomas.StockAnalyze.activity.base.BaseFragmentActivity;
import cz.tomas.StockAnalyze.ui.widgets.DragContainerView;
import cz.tomas.StockAnalyze.utils.NavUtils;

public abstract class AbstractStocksActivity extends BaseFragmentActivity {

	public interface IDragSupportingActivity<T> {
		void onStartDrag(T data, View view, DragContainerView.IDragListener listener);
	}
	
	protected UpdateScheduler scheduler;
	protected DataManager dataManager;
	
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
	
	protected final IMarketListener marketListener = new IMarketListener() {
		@Override
		public void onMarketsAvailable(Market[] markets) {
			onPrepareData(markets);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.scheduler = (UpdateScheduler) this.getApplicationContext().getSystemService(Application.UPDATE_SCHEDULER_SERVICE);
		this.dataManager = (DataManager) getApplicationContext().getSystemService(Application.DATA_MANAGER_SERVICE);
	}

	@Override
	public void onStart() {
		super.onStart();
		this.scheduler.addListener(this.updateListener);
		this.dataManager.addMarketListener(this.marketListener);
	}

	@Override
	public void onStop() {
		super.onStop();
		this.scheduler.removeListener(this.updateListener);
		this.dataManager.removeMarketListener(this.marketListener);
	}

	protected void updateImmediately(Market market) {
		this.scheduler.updateImmediately(market);
	}

	@Override
	protected void onNavigateUp() {
		NavUtils.goUp(this, HomeActivity.class);
	}

	protected void onPrepareData(Market[] markets) {
	}
}