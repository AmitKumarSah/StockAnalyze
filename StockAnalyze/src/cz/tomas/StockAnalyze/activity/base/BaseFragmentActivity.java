package cz.tomas.StockAnalyze.activity.base;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.flurry.android.FlurryAgent;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.activity.IndecesListActivity;
import cz.tomas.StockAnalyze.activity.NewsActivity;
import cz.tomas.StockAnalyze.activity.PortfoliosActivity;
import cz.tomas.StockAnalyze.activity.StocksActivity;
import cz.tomas.StockAnalyze.fragments.ProgressDialogFragment;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

public class BaseFragmentActivity extends SherlockFragmentActivity {

	protected static final String TAG_PROGRESS = "progress";
	protected static final String TAG_CONFIRM = "confirm";

	protected static final int NAVIGATION_STOCKS = 0;
	protected static final int NAVIGATION_INDICES = 1;
	protected static final int NAVIGATION_PORTFOLIO = 2;
	protected static final int NAVIGATION_NEWS = 3;

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

	protected void setAsTopLevelActivity(int position) {
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		SpinnerAdapter navigationListAdapter = ArrayAdapter.createFromResource(this, R.array.navigation_list,
				android.R.layout.simple_spinner_dropdown_item);
		getSupportActionBar().setListNavigationCallbacks(navigationListAdapter, new NavigationListener());
		getSupportActionBar().setSelectedNavigationItem(position);
	}

	class NavigationListener implements ActionBar.OnNavigationListener {

		@Override
		public boolean onNavigationItemSelected(int itemPosition, long itemId) {
			if (BaseFragmentActivity.this.isFinishing()) {
				return false;
			}
			Class<? extends Activity> clazz = null;
			switch (itemPosition) {
				case NAVIGATION_STOCKS:
					clazz = StocksActivity.class;
					break;
				case NAVIGATION_INDICES:
					clazz = IndecesListActivity.class;
					break;
				case NAVIGATION_PORTFOLIO:
					clazz = PortfoliosActivity.class;
					break;
				case NAVIGATION_NEWS:
					clazz = NewsActivity.class;
					break;

			}
			Intent intent = new Intent(BaseFragmentActivity.this, clazz);
			startActivity(intent);
			return true;
		}
	}
}
