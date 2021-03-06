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
package cz.tomas.StockAnalyze.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.UpdateScheduler;
import cz.tomas.StockAnalyze.utils.Utils;

public class AlarmReceiver extends BroadcastReceiver {

	/**
	 * schedule next update and do an immediate update
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		boolean intra = intent.getExtras().getBoolean(UpdateScheduler.ARG_INTRA, true);
		UpdateScheduler scheduler = (UpdateScheduler) context.getSystemService(Application.UPDATE_SCHEDULER_SERVICE);
		if (intra) {
			scheduler.scheduleNextIntraDayUpdate();
		} else {
			//scheduler.scheduleNextDayUpdate();
		}
		
		try {
			scheduler.performScheduledUpdate();
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to perform scheduled update", e);
		}
	}

}
