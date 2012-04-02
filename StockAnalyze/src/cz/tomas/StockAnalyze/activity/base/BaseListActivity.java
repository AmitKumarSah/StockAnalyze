package cz.tomas.StockAnalyze.activity.base;

import android.app.ActionBar;
import android.util.Log;
import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.flurry.android.FlurryAgent;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

public abstract class BaseListActivity extends SherlockListActivity {

	public void onStart() {
		super.onStart();
		long kb = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L;
		Log.i(Utils.LOG_TAG, "allocation [kb]: " + kb);
		FlurryAgent.onStartSession(this, Utils.FLURRY_KEY);
		FlurryAgent.onPageView();
	}
	
	public void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
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
