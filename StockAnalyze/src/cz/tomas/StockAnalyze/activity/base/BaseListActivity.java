package cz.tomas.StockAnalyze.activity.base;

import com.flurry.android.FlurryAgent;

import cz.tomas.StockAnalyze.utils.Utils;
import android.app.ListActivity;
import android.util.Log;

public abstract class BaseListActivity extends ListActivity {

	public void onStart()
	{
		super.onStart();
		long kb = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L;
		Log.i(Utils.LOG_TAG, "allocation [kb]: " + kb);
		FlurryAgent.onStartSession(this, Utils.FLURRY_KEY);
		FlurryAgent.onPageView();
	}
	
	public void onStop()
	{
		super.onStop();
		FlurryAgent.onEndSession(this);
	}
}
