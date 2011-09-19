/*******************************************************************************
 * StockAnalyze for Android
 *     Copyright (C)  2011 Tomas Vondracek.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/**
 * 
 */
package cz.tomas.StockAnalyze;

import java.util.Calendar;
import java.util.concurrent.Semaphore;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.util.Log;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.DataProviderFactory;
import cz.tomas.StockAnalyze.Data.IStockDataProvider;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.receivers.AlarmReceiver;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.Markets;
import cz.tomas.StockAnalyze.utils.Utils;

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
	private final int INTRADAY_START_UPDATE_HOUR = 9;
	
	private boolean isSchedulerRunning = false;
	
	private static PendingIntent intraUpdateIntent;
	private static PendingIntent dayUpdateIntent;
	
	private Semaphore semaphore;
	
	UpdateScheduler(Context context) {
		this.context = context;
		this.semaphore = new Semaphore(1);
		
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
					if (sharedPreferences.getBoolean(key, true)) {
						perfromScheduledUpdate();
					}
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
		this.scheduleNextIntraDayUpdate(Markets.CZ);
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
	 * perform update scheduled by alarm,
	 * send event to flurry analysis
	 */
	public void perfromScheduledUpdate() {
		Log.i(Utils.LOG_TAG, "going to perform scheduled update");
//		FlurryAgent.onStartSession(this.context, "UpdateSheduler");
//		Map<String, String> pars = new HashMap<String, String>();
//		pars.put(Consts.FLURRY_KEY_SCHEDULED_UPDATE_DAY, String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)));
//		FlurryAgent.onEvent(Consts.FLURRY_EVENT_SCHEDULED_UPDATE, pars);
		
		this.performUpdateInternal(Markets.CZ);
		this.performUpdateInternal(Markets.DE);
		this.performUpdateInternal(Markets.GLOBAL);
//		FlurryAgent.onEndSession(this.context);
	}
	
	/**
	 * update real time data immediately for all markets
	 */
	public void updateImmediatly() {
		this.performUpdateInternal(Markets.CZ);
		this.performUpdateInternal(Markets.DE);
		this.performUpdateInternal(Markets.GLOBAL);
	}
	
	/**
	 * update real time data immediately for given market
	 */
	public void updateImmediatly(Market market) {
		this.performUpdateInternal(market);
	}

	/**
	 * execute refresh task on realtime provider
	 */
	private void performUpdateInternal(Market market) {
		if (! Utils.isOnline(this.context)) {
			Log.i(Utils.LOG_TAG, "Device is offline, canceling data update");
			return;
		}
		this.isSchedulerRunning = true;
		if (!DataManager.isInitialized()) {
			// if process was started by alarm and the rest of the application isn't initialized, 
			// we need to initialize DataManager
			DataManager.getInstance(this.context);
		}
		IStockDataProvider provider = DataProviderFactory.getRealTimeDataProvider(market);

		RefreshTask task = new RefreshTask();
		task.execute(provider);
	}
	
	/**
	 * schedule next update via Alarm and according to preferences.
	 * Scheduling also takes takes cares about days without trading (e.g. weekends)
	 */
	private void scheduleAlarm(boolean intraDay) {
		// get a Calendar object with current time
		Calendar cal = Calendar.getInstance(Utils.PRAGUE_TIME_ZONE);
		int today = cal.get(Calendar.DAY_OF_YEAR);
		cal = Utils.getNextValidDate(cal);
		PendingIntent pendingIntent = null;
		if (intraDay) {
			// if calendar was moved forward (from weekend to Monday),
			// set update time to morning 
			if (today == cal.get(Calendar.DAY_OF_YEAR)) {
				int seconds = this.preferences.getInt(Utils.PREF_INTERVAL_BACKGROUND_UPDATE, DEFAULT_REFRESH_INTERVAL) * 60;
				cal.add(Calendar.SECOND, seconds);
			} else {
				cal.set(Calendar.HOUR_OF_DAY, INTRADAY_START_UPDATE_HOUR);
				cal.set(Calendar.MINUTE, 5);
			}
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
					semaphore.acquire();	// synchronize updates, so there is only one active at the time (because backend instances)
					IStockDataProvider provider = params[0];
					if(provider != null) {
						Log.d(Utils.LOG_TAG, provider.getDescriptiveName() + ": initiating provider refresh in UpdateScheduler");
						return provider.refresh();
					}
					else
						throw new NullPointerException("IStockDataProvider is null");
				} catch (Exception e) {
					Log.e(Utils.LOG_TAG, "failed to refresh for provider", e);
				} finally {
					semaphore.release();
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
