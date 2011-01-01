package cz.tomas.StockAnalyze;

import cz.tomas.StockAnalyze.utils.Utils;
import android.app.ActivityManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AppBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(Utils.LOG_TAG, "background update changed " + intent.toString());
	}

}
