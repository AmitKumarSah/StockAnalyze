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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
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
import cz.tomas.StockAnalyze.Data.Interfaces.IMarketListener;
import cz.tomas.StockAnalyze.Data.Interfaces.IUpdateSchedulerListener;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.receivers.AlarmReceiver;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.Markets;
import cz.tomas.StockAnalyze.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Semaphore;

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
public class UpdateScheduler implements IMarketListener{

	public static final String ARG_INTRA = "intraday";
	
	private static final int DEFAULT_REFRESH_INTERVAL = 120;		//minutes
	private static final int REQUEST_CODE = 13215564;
	
	public static final String INTRA_UPDATE_ACTION = "cz.tomas.StockAnalyze.INTRADAY_DATA_UPDATE";
	public static final String DAY_UPDATE_ACTION = "cz.tomas.StockAnalyze.DAY_DATA_UPDATE";
	
	private static final int DAY_UPDATE_HOUR = 18;
	private static final int INTRADAY_START_UPDATE_HOUR = 9;

	private final Context context;
	private final SharedPreferences preferences;

	private final PendingIntent intraUpdateIntent;
	private final PendingIntent dayUpdateIntent;
	private final List<IUpdateSchedulerListener> listeners;
	private Market[] markets;
	
	private Semaphore semaphore;
	
	private boolean isUpdateAwaiting;
	
	UpdateScheduler(Context context) {
		this.context = context;
		this.semaphore = new Semaphore(1);
		this.listeners = new ArrayList<IUpdateSchedulerListener>();
		
		Intent intent = new Intent(INTRA_UPDATE_ACTION, null, this.context, AlarmReceiver.class);
		intent.putExtra(ARG_INTRA, true);
		intraUpdateIntent = PendingIntent.getBroadcast(this.context,
				REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		intent = new Intent(DAY_UPDATE_ACTION, null, this.context, AlarmReceiver.class);
		intent.putExtra(ARG_INTRA, false);
		dayUpdateIntent = PendingIntent.getBroadcast(this.context,
				REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		this.preferences = context.getSharedPreferences(Utils.PREF_NAME, 0);
		
		this.preferences.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				// if there is change in update preferences, do an update or schedule new one
				if (key.equals(Utils.PREF_ENABLE_BACKGROUND_UPDATE)) {
					if (sharedPreferences.getBoolean(key, true)) {
						performScheduledUpdate();
					}
					scheduleNextIntraDayUpdate();
				} else if (key.equals(Utils.PREF_INTERVAL_BACKGROUND_UPDATE)) {
					scheduleNextIntraDayUpdate();
				}
					
			}
		});
	}
	
	/**
	 * check whether the scheduler is currently actively updating data
	 * @return true if scheduler is currently performing an update
	 */
	boolean isSchedulerRunning() {
		return this.semaphore.availablePermits() == 0;
	}
	
	/**
	 * Schedule next update with real time data provider for given market,
	 * if it is enabled in preferences
	 */
	public void scheduleNextIntraDayUpdate() {
		boolean enabled = this.preferences.getBoolean(Utils.PREF_ENABLE_BACKGROUND_UPDATE, Utils.PREF_DEF_ENABLE_BACKGROUND_UPDATE);
		if (enabled) {
			this.scheduleAlarm(true);
		}
	}
	
	/**
	 * perform update scheduled by alarm,
	 * send event to flurry analysis
	 */
	public void performScheduledUpdate() {
		if (markets == null) {
			this.isUpdateAwaiting = true;
			// if we are starting application and markets haven't been loaded yet, we need to wait for them
			Log.d(Utils.LOG_TAG, "cannot perform scheduled update now, because we don't have loaded markets");
			return;
		}
		final boolean isSynchronizationEnabled = ContentResolver.getMasterSyncAutomatically();
		if (! isSchedulerRunning() && isSynchronizationEnabled) {
			Log.i(Utils.LOG_TAG, "going to perform scheduled update");

			for (IUpdateSchedulerListener listener : this.listeners) {
				if (listener != null) {
					listener.onUpdateBegin(markets);
				}
			}

			for (Market market : markets) {
				this.performUpdateInternal(market);
			}
			this.performUpdateInternal(Markets.GLOBAL);
		} else {
			Log.i(Utils.LOG_TAG, "skipping scheduled update because one is already running");
		}
	}
	
	/**
	 * update real time data immediately for all markets
	 */
	public void updateImmediately() {
		if (markets == null) {
			this.isUpdateAwaiting = true;
			Log.d(Utils.LOG_TAG, "cannot do immediate update, because we don't have loaded markets");
			return;
		}
		for (IUpdateSchedulerListener listener : this.listeners) {
			if (listener != null) {
				listener.onUpdateBegin(markets);
			}
		}

		for (Market market : markets) {
			this.performUpdateInternal(market);
		}
	}
	
	/**
	 * update real time data immediately for given market
	 *
	 * @param market market to perform update on
	 */
	public void updateImmediately(Market market) {
		for (IUpdateSchedulerListener listener : this.listeners) {
			if (listener != null) {
				listener.onUpdateBegin(market);
			}
		}
		this.performUpdateInternal(market);
	}

	/**
	 * execute refresh task on real time provider
	 *
	 * @param market market to perform update on
	 */
	private void performUpdateInternal(Market market) {
		if (! Utils.isOnline(this.context)) {
			Log.i(Utils.LOG_TAG, "Device is offline, canceling data update");
			return;
		}
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
	 *
	 * @param intraDay true if this is intraday update
	 */
	private void scheduleAlarm(boolean intraDay) {
		// get a Calendar object with current time
		Calendar cal = Calendar.getInstance(Utils.PRAGUE_TIME_ZONE);
		final int today = cal.get(Calendar.DAY_OF_YEAR);
		cal = Utils.getNextValidDate(cal);
		PendingIntent pendingIntent;
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
			if (cal.get(Calendar.HOUR_OF_DAY) >= DAY_UPDATE_HOUR) {
				cal.add(Calendar.HOUR_OF_DAY, 24);
			}
			cal.set(Calendar.HOUR_OF_DAY, DAY_UPDATE_HOUR);
			cal.set(Calendar.MINUTE,1);
			pendingIntent = dayUpdateIntent;
		}
		Log.d(Utils.LOG_TAG, "SCHEDULING " + (intraDay? "intra" : "day") + " ALARM TO " + FormattingUtils.formatStockDate(cal));

		// Get the AlarmManager service
		final AlarmManager am = (AlarmManager) this.context.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC, cal.getTimeInMillis(), pendingIntent);
	}
	
	public void addListener(IUpdateSchedulerListener listener) {
		this.listeners.add(listener);
	}
	
	public boolean removeListener(IUpdateSchedulerListener listener) {
		return this.listeners.remove(listener);
	}

	@Override
	public void onMarketsAvailable(Market[] markets) {
		Log.d(Utils.LOG_TAG, String.format("received markets %s, performing update = %b", markets, isUpdateAwaiting));
		this.markets = markets;
		if (isUpdateAwaiting) {
			isUpdateAwaiting = false;
			this.updateImmediately();
		}
	}

	/**
	 * do the refresh on given StockDataProvider
	 */
	class RefreshTask extends AsyncTask<IStockDataProvider, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(IStockDataProvider... params) {
			if (params.length == 1)
				try {
					semaphore.acquire();	// synchronize updates, so there is only one active at the time (because backend instances)
					final IStockDataProvider provider = params[0];
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
			return null;
		}

		/**
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			final Editor editor = preferences.edit();
			editor.putLong(Utils.PREF_LAST_UPDATE_TIME, System.currentTimeMillis());
			editor.commit();
			
			final boolean succeeded = result != null;
			
			for (IUpdateSchedulerListener listener : listeners) {
				if (listener != null) {
					listener.onUpdateFinished(succeeded);
				}
			}
		}
		
	}
}
