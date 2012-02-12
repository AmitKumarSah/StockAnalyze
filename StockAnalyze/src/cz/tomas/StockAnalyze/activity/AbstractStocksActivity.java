package cz.tomas.StockAnalyze.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Interfaces.IMarketListener;
import cz.tomas.StockAnalyze.Data.Interfaces.IUpdateSchedulerListener;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.UpdateScheduler;
import cz.tomas.StockAnalyze.activity.base.BaseFragmentActivity;
import cz.tomas.StockAnalyze.fragments.ProgressDialogFragment;
import cz.tomas.StockAnalyze.utils.NavUtils;

public abstract class AbstractStocksActivity extends BaseFragmentActivity {

	private static final String TAG_PROGRESS = "progress";

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

	/**
	 * Immediately show {@link ProgressDialogFragment} with given texts. You can
	 * access dialog instance later by finding fragment with tag
	 * {@link #TAG_PROGRESS}
	 *
	 * @param titleId
	 * @param messageId
	 * @return
	 */
	protected DialogFragment showProgressDialog(int titleId, int messageId, DialogInterface.OnCancelListener listener) {
		ProgressDialogFragment newFragment = ProgressDialogFragment.newInstance(
				titleId, messageId);

		newFragment.setCancelable(true);
		newFragment.show(getSupportFragmentManager(), TAG_PROGRESS);
		newFragment.setCancelListener(listener);

		return newFragment;
	}

	/**
	 * Dismiss dialog previously created with {@link #showProgressDialog(int, int, DialogInterface.OnCancelListener)}.
	 * If there wasn't any dialog, nothing happens.
	 */
	protected void dismissProgress() {
		try {
			DialogFragment fragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(TAG_PROGRESS);
			if (fragment != null) {
				fragment.dismiss();
			}
		} catch (Exception e) {
			// nothing
		}
	}
}