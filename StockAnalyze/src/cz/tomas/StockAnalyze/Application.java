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
package cz.tomas.StockAnalyze;

import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.activity.ChartActivity;
import cz.tomas.StockAnalyze.utils.Utils;
import android.util.Log;

public class Application extends android.app.Application {

	public static final String UPDATE_SCHEDULER_SERVICE = "cz.tomas.StockAnalyze.Data.UpdateScheduler"; 
	public static final String DATA_MANAGER_SERVICE = "cz.tomas.StockAnalyze.Data.DataManager";
	
	private UpdateScheduler scheduler;
	private DataManager dataManager;
	
	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		Log.i(Utils.LOG_TAG, "STARTING THE APPLICATION");
		
		this.scheduler = new UpdateScheduler(this);
		this.dataManager = DataManager.getInstance(this);

		// do immediate update and schedule next one
		try {
			if (! this.scheduler.isSchedulerRunnig()) {
				this.scheduler.updateImmediatly();
				this.scheduler.scheduleNextIntraDayUpdate();
				this.scheduler.scheduleNextDayUpdate();
			}
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "Failed to schedule updates!", e);
		}
		
		super.onCreate();
	}

	/* (non-Javadoc)
	 * @see android.content.ContextWrapper#getSystemService(java.lang.String)
	 */
	@Override
	public Object getSystemService(String name) {
		if (name.equals(UPDATE_SCHEDULER_SERVICE))
			return this.scheduler;
		else if (name.equals(DATA_MANAGER_SERVICE))
			return this.dataManager;
		return super.getSystemService(name);
	}

	/* (non-Javadoc)
	 * @see android.app.Application#onLowMemory()
	 */
	@Override
	public void onLowMemory() {
		Log.i(Utils.LOG_TAG, "low MEMORY... clearing caches...");
		ChartActivity.clearCache();
		super.onLowMemory();
	}



	/* (non-Javadoc)
	 * @see android.app.Application#onTerminate()
	 */
	@Override
	public void onTerminate() {
		Log.i(Utils.LOG_TAG, "Application is going DOWN!");
		super.onTerminate();
	}
	
	
}
