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

	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			boolean intra = intent.getExtras().getBoolean("intraday", true);
			if (intra)
				UpdateScheduler.getInstance(context).scheduleNextIntraDayUpdate();
			else
				UpdateScheduler.getInstance(context).scheduleNextDayUpdate();
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
