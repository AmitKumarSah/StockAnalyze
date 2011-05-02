/**
 * 
 */
package cz.tomas.StockAnalyze.Data;

import java.util.Calendar;

import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.receivers.AlarmReceiver;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.Utils;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author tomas
 *
 */
public class UpdateScheduler {
	
	private Context context;
	private SharedPreferences preferences;
	
	private static UpdateScheduler instance;
	
	private final int DEFAULT_REFRESH_INTERVAL = 10;		//minutes
	private final int REQUEST_CODE = 13215564;
	
	/*
	 * singleton
	 */
	public static UpdateScheduler getInstance(Context context) {
		if (instance == null)
			instance = new UpdateScheduler(context);
		return instance;
	}
	
	private UpdateScheduler(Context context) {
		this.context = context;
		this.preferences = context.getSharedPreferences(Utils.PREF_NAME, 0);
		
		this.preferences.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				// if there is change in update preferences, do an update or schedule new one
				if (key.equals(Utils.PREF_ENABLE_BACKGROUND_UPDATE)) {
					if (sharedPreferences.getBoolean(key, true))
						updateImmediatly();
					scheduleNextIntraDayUpdate();
				}
				else if (key.equals(Utils.PREF_INTERVAL_BACKGROUND_UPDATE)) {
					scheduleNextIntraDayUpdate();
				}
					
			}
		});
	}
	
	/*
	 * Schedule next update with real time data provider,
	 * if it is enabled in preferences
	 */
	public void scheduleNextIntraDayUpdate() {
		this.scheduleNextIntraDayUpdate(MarketFactory.getCzechMarket());
	}
	
	/*
	 * Schedule next update with real time data provider for given market,
	 * if it is enabled in preferences
	 */
	public void scheduleNextIntraDayUpdate(Market market) {
		boolean enabled = this.preferences.getBoolean(Utils.PREF_ENABLE_BACKGROUND_UPDATE, true);
		if (enabled)
			this.scheduleAlarm(true);
	}
	
	/*
	 * schedule next update with historical data provider
	 */
	public void scheduleNextDayUpdate() {
//		IStockDataProvider provider = DataProviderFactory.getHistoricalDataProvider(MarketFactory.getCzechMarket());
//		
//		RefreshTask task = new RefreshTask();
//		task.execute(provider);
		// TODO
	}
	
	/*
	 * update real time data immediately
	 */
	public void updateImmediatly() {
		if (! Utils.isOnline(this.context)) {
			Log.i(Utils.LOG_TAG, "Device is offline, canceling data update");
			return;
		}
		if (!DataManager.isInitialized()) {
			DataManager.getInstance(this.context);
		}
		IStockDataProvider provider = DataProviderFactory.getRealTimeDataProvider(MarketFactory.getCzechMarket());
		
		RefreshTask task = new RefreshTask();
		task.execute(provider);
	}
	
	/*
	 * schedule next update via Alarm and according to preferences
	 */
	private void scheduleAlarm(boolean intraDay) {
		// get a Calendar object with current time
		Calendar cal = Calendar.getInstance();
		int seconds = this.preferences.getInt(Utils.PREF_INTERVAL_BACKGROUND_UPDATE, DEFAULT_REFRESH_INTERVAL) * 60;
		cal.add(Calendar.SECOND, seconds);
		
		Log.d(Utils.LOG_TAG, "scheduling alarm to " + FormattingUtils.formatStockDate(cal));
		
		Intent intent = new Intent(this.context, AlarmReceiver.class);
		intent.putExtra("intraday", intraDay);
		PendingIntent sender = PendingIntent.getBroadcast(this.context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC, cal.getTimeInMillis(), sender);
	}
	
	/*
	 * do the refresh on given StockDataProvider
	 */
	class RefreshTask extends AsyncTask<IStockDataProvider, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(IStockDataProvider... params) {
			if (params.length == 1)
				try {
					IStockDataProvider provider = params[0];
					if(provider != null) {
						Log.d(Utils.LOG_TAG, provider.getDescriptiveName() + ": initiating provider refresh in UpdateScheduler");
						return provider.refresh();
					}
					else
						throw new NullPointerException("IStockDataProvider is null");
				} catch (Exception e) {
					Log.e(Utils.LOG_TAG, "failed to refresh for provider", e);
				}
			return null;
		}
		
	}
}
