package cz.tomas.StockAnalyze.activity.base;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.flurry.android.FlurryAgent;
import cz.tomas.StockAnalyze.fragments.ProgressDialogFragment;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

public class BaseFragmentActivity extends SherlockFragmentActivity {

	protected static final String TAG_PROGRESS = "progress";
	protected static final String TAG_CONFIRM = "confirm";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	public void onStart() {
		long kb = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L;
		Log.i(Utils.LOG_TAG, "allocation [kb]: " + kb);
		super.onStart();
		FlurryAgent.onStartSession(this, Utils.FLURRY_KEY);
		FlurryAgent.onPageView(); 
	}
	
	public void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}


	/**
	 * Immediately show {@link cz.tomas.StockAnalyze.fragments.ProgressDialogFragment} with given texts. You can
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
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if (isDisplayedUp()) {
				this.onNavigateUp();
			} else {
				NavUtils.goHome(this);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void onNavigateUp() {
		NavUtils.goHome(this);
	}

	protected boolean isDisplayedUp() {
		return (getSupportActionBar().getDisplayOptions() & ActionBar.DISPLAY_HOME_AS_UP)
				== ActionBar.DISPLAY_HOME_AS_UP;
	}
}
