package cz.tomas.StockAnalyze.activity.base;

import android.util.Log;

import com.flurry.android.FlurryAgent;

import cz.tomas.StockAnalyze.utils.Utils;

public class BaseFragmentActivity extends ActionBarFragmentActivity {
	
	public void onStart()
	{
		long kb = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L;
		Log.i(Utils.LOG_TAG, "allocation [kb]: " + kb);
		super.onStart();
		FlurryAgent.onStartSession(this, Utils.FLURRY_KEY);
		FlurryAgent.onPageView(); 
	}
	
	public void onStop()
	{
		super.onStop();
		FlurryAgent.onEndSession(this);
	}
}
