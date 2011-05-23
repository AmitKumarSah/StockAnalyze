/**
 * 
 */
package cz.tomas.StockAnalyze;

import java.util.Calendar;

import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.DataProviderFactory;
import cz.tomas.StockAnalyze.Data.IStockDataProvider;
import cz.tomas.StockAnalyze.Data.MarketFactory;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.receivers.AlarmReceiver;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.Utils;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.util.Log;

/**
 * UpdateScheduler service. allows to schedule different updates,
 * according to application preferences and also invoke data update.
 * This class is instantiated in Application class
 * and is accessible as system service: 
 * <p><code>
 * UpdateScheduler scheduler = (UpdateScheduler) context.getSystemService(Application.UPDATE_SCHEDULER_SERVICE);
 * </code>
 * </p>
 * 
 * This class uses the same PendingIntent for sheduling to ensure,
 * there aren't more scheduled alarms of the same type
 * 
 * @author tomas
 *
 */
public class UpdateScheduler {
	
	private Context context;
	private SharedPreferences preferences;
	
	private final int DEFAULT_REFRESH_INTERVAL = 10;		//minutes
	private final int REQUEST_CODE = 13215564;
	
	public static final String INTRA_UPDATE_ACTION = "cz.tomas.StockAnalyze.INTRADAY_DATA_UPDATE";
	public static final String DAY_UPDATE_ACTION = "cz.tomas.StockAnalyze.DAY_DATA_UPDATE";
	
	private final int DAY_UPDATE_HOUR = 18;
	
	private boolean isSchedulerRunning = false;
	
	private static PendingIntent intraUpdateIntent;
	private static PendingIntent dayUpdateIntent;
	
	UpdateScheduler(Context context) {
		this.context = context;
		
		if (intraUpdateIntent == null) {
			Intent intent = new Intent(INTRA_UPDATE_ACTION, null, this.context, AlarmReceiver.class);
			intent.putExtra("intraday", true);
			intraUpdateIntent = PendingIntent.getBroadcast(this.context,
					REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		if (dayUpdateIntent == null) {
			Intent intent = new Intent(DAY_UPDATE_ACTION, null, this.context, AlarmReceiver.class);
			intent.putExtra("intraday", false);
			dayUpdateIntent = PendingIntent.getBroadcast(this.context,
					REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		}
		Log.i(Utils.LOG_TAG, "update intents equality: " + dayUpdateIntent.equals(intraUpdateIntent));
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
	
	/**
	 * check whether the scheduler is currently actively updating data
	 * @return
	 */
	boolean isSchedulerRunnig() {
		return isSchedulerRunning;
	}

	/**
	 * Schedule next update with real time data provider,
	 * if it is enabled in preferences
	 */
	public void scheduleNextIntraDayUpdate() {
		this.scheduleNextIntraDayUpdate(MarketFactory.getCzechMarket());
	}
	
	/**
	 * Schedule next update with real time data provider for given market,
	 * if it is enabled in preferences
	 */
	public void scheduleNextIntraDayUpdate(Market market) {
		boolean enabled = this.preferences.getBoolean(Utils.PREF_ENABLE_BACKGROUND_UPDATE, true);
		if (enabled)
			this.scheduleAlarm(true);
	}
	
	/**
	 * schedule next update with historical data provider
	 */
	public void scheduleNextDayUpdate() {
		//if (! this.isDayUpdateScheduled)
			this.scheduleAlarm(false);
	}
	
	/**
	 * update real time data immediately
	 */
	public void updateImmediatly() {
		if (! Utils.isOnline(this.context)) {
			Log.i(Utils.LOG_TAG, "Device is offline, canceling data update");
			return;
		}
		this.isSchedulerRunning = true;
		if (!DataManager.isInitialized()) {
			DataManager.getInstance(this.context);
		}
		IStockDataProvider provider = DataProviderFactory.getRealTimeDataProvider(MarketFactory.getCzechMarket());

		RefreshTask task = new RefreshTask();
		task.execute(provider);
	}
	
	/**
	 * schedule next update via Alarm and according to preferences
	 */
	private void scheduleAlarm(boolean intraDay) {
		// get a Calendar object with current time
		Calendar cal = Calendar.getInstance(Utils.PRAGUE_TIME_ZONE);
		PendingIntent pendingIntent = null;
		if (intraDay) {
			int seconds = this.preferences.getInt(
					Utils.PREF_INTERVAL_BACKGROUND_UPDATE,
					DEFAULT_REFRESH_INTERVAL) * 60;
			cal.add(Calendar.SECOND, seconds);
			pendingIntent = intraUpdateIntent;
		} else {
			// set it for tomorrow evening
			if (cal.get(Calendar.HOUR_OF_DAY) >= DAY_UPDATE_HOUR)
				cal.add(Calendar.HOUR_OF_DAY, 24);
			cal.set(Calendar.HOUR_OF_DAY, DAY_UPDATE_HOUR);
			cal.set(Calendar.MINUTE,1);
			pendingIntent = dayUpdateIntent;
		}
		Log.d(Utils.LOG_TAG, "SHEDULING " + (intraDay? "intraday" : "day") + " ALARM TO " + FormattingUtils.formatStockDate(cal));
			

		// Get the AlarmManager service
		AlarmManager am = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC, cal.getTimeInMillis(), pendingIntent);
	}
	
	/**
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
			isSchedulerRunning = false;
			return null;
		}

		/**
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Boolean result) {
			Editor editor = preferences.edit();
			editor.putLong(Utils.PREF_LAST_UPDATE_TIME, System.currentTimeMillis());
			editor.commit();
			super.onPostExecute(result);
		}
		
	}
}
