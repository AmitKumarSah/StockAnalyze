package cz.tomas.StockAnalyze.receivers;

import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.UpdateScheduler;
import cz.tomas.StockAnalyze.utils.Utils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

	/*
	 * schedule next update and do an update
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		boolean intra = intent.getExtras().getBoolean("intraday", true);
		UpdateScheduler scheduler = UpdateScheduler.getInstance(context);
		if (intra)
			scheduler.scheduleNextIntraDayUpdate();
		else
			scheduler.scheduleNextDayUpdate();
		
		try {
			scheduler.updateImmediatly();
		} catch (Exception e) {
			e.printStackTrace();
			String message = context.getString(R.string.failedScheduleUpdate);
			if (e.getMessage() != null)
				message += "\n" + e.getMessage();
			
			Log.d(Utils.LOG_TAG, message);
			Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
		}
	}

}