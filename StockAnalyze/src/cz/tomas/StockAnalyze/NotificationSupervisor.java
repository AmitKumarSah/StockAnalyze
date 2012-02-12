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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import cz.tomas.StockAnalyze.Data.Interfaces.IUpdateSchedulerListener;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.activity.StocksActivity;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.Utils;

import java.util.Calendar;

/**
 * Supervisor over notifications from application
 * 
 * @author tomas
 *
 */
public class NotificationSupervisor implements IUpdateSchedulerListener {

	private Context context;
	private final NotificationManager notificationManager;
	private StringBuilder stringBuilder;
	private final CharSequence updateBeginMessage;
	private final CharSequence updateFinishedMessage;
	private final CharSequence noUpdateMessage;
	private RemoteViews currentNotificationView;
	
	private Notification notification;
	private PendingIntent contentIntent;
	final SharedPreferences pref;
	
	private static final int UPDATE_DATA_ID = 1;
	
	public NotificationSupervisor(Context context) {
		this.context = context;
		this.notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
		this.stringBuilder = new StringBuilder();
		this.updateBeginMessage = this.context.getText(R.string.dataUpdating);
		this.updateFinishedMessage = this.context.getText(R.string.dataUpdated);
		this.noUpdateMessage = this.context.getText(R.string.noDataUpdated);
		this.pref = this.context.getSharedPreferences(Utils.PREF_NAME, 0);
	}

	private void showStartNotification(Market... markets) {
		final boolean enableNotif = this.pref.getBoolean(Utils.PREF_UPDATE_NOTIF,  Utils.PREF_DEF_UPDATE_NOTIF);
		
		if (markets == null || ! enableNotif) {
			return;
		}
		if (this.currentNotificationView == null) {
			this.currentNotificationView = new RemoteViews(this.context.getPackageName(), R.layout.custom_update_notification_layout);
		}
		this.currentNotificationView.setImageViewResource(R.id.notification_image, R.drawable.ic_stat_arrow);
		this.stringBuilder.setLength(0);
		this.stringBuilder.append(this.updateBeginMessage);
		this.stringBuilder.append(" ");
		int index = 0;
		for (Market market : markets) {
			if (market != null) {
				if (index > 0) {
					this.stringBuilder.append(", ");
				}
				this.stringBuilder.append(market.getId());
				index++;
			}
		}

		this.notification = new Notification(R.drawable.ic_stat_arrow, this.updateBeginMessage, System.currentTimeMillis());
		this.notification.flags |= Notification.FLAG_AUTO_CANCEL;
		//this.notification.contentView = this.currentNotificationView;
		
		// set intent to launch when the notification is tapped
		Intent notificationIntent = new Intent(this.context, StocksActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		this.contentIntent = PendingIntent.getActivity(this.context, 0, notificationIntent, 0);
		this.notification.setLatestEventInfo(this.context, this.context.getText(R.string.app_name), this.stringBuilder.toString(), contentIntent);
		
		notificationManager.notify(UPDATE_DATA_ID, this.notification);
	}

	private void showFinishNotification() {
		if (this.currentNotificationView != null) {
			this.stringBuilder.setLength(0);
			this.stringBuilder.append(this.updateFinishedMessage);
			this.stringBuilder.append(": ");
			this.stringBuilder.append(FormattingUtils.formatStockDate(Calendar.getInstance()));
			

			if (this.pref.getBoolean(Utils.PREF_PERMANENT_NOTIF, Utils.PREF_DEF_PERMANENT_NOTIF)) {
				//this.currentNotificationView.setTextViewText(R.id.notification_text, this.stringBuilder.toString());
				this.notification.setLatestEventInfo(this.context, this.context.getText(R.string.app_name), this.stringBuilder.toString(), this.contentIntent);
				this.notificationManager.notify(UPDATE_DATA_ID, this.notification);
			} else {
				this.notificationManager.cancel(UPDATE_DATA_ID);
			}
		}
	}

	private void showNoUpdateNotification() {
		if (this.currentNotificationView != null) {
			this.stringBuilder.setLength(0);
			this.stringBuilder.append(this.noUpdateMessage);
			this.stringBuilder.append(": ");
			this.stringBuilder.append(FormattingUtils.formatStockDate(Calendar.getInstance()));
			
			final boolean permNotif = this.context.getSharedPreferences(Utils.PREF_NAME, 0).getBoolean(Utils.PREF_PERMANENT_NOTIF, true);
			if (permNotif) {
				//this.currentNotificationView.setTextViewText(R.id.notification_text, this.stringBuilder.toString());
				this.notification.setLatestEventInfo(this.context, this.context.getText(R.string.app_name), this.stringBuilder.toString(), this.contentIntent);
				this.notificationManager.notify(UPDATE_DATA_ID, this.notification);
			} else {
				this.notificationManager.cancel(UPDATE_DATA_ID);
			}
		}
	}

	@Override
	public void onUpdateBegin(Market... markets) {
		this.showStartNotification(markets);
	}

	@Override
	public void onUpdateFinished(boolean success) {
		if (success) {
			this.showFinishNotification();
		} else {
			this.showNoUpdateNotification();
		}
	}

}
