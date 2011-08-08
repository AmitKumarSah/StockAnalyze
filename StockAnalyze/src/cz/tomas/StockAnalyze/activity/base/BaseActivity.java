package cz.tomas.StockAnalyze.activity.base;

import com.flurry.android.FlurryAgent;

import cz.tomas.StockAnalyze.utils.Utils;

import android.app.Activity;

public abstract class BaseActivity extends Activity {

	public void onStart()
	{
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
