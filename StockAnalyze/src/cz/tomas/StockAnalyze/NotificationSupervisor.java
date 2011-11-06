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
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import cz.tomas.StockAnalyze.Data.IStockDataProvider;
import cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener;
import cz.tomas.StockAnalyze.Data.Interfaces.IUpdateSchedulerListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.activity.StocksActivity;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * Supervisor over notifications from application
 * 
 * @author tomas
 *
 */
public class NotificationSupervisor implements IUpdateSchedulerListener, IStockDataListener {

	private Context context;
	private NotificationManager notificationManager;
	private StringBuilder stringBuilder;
	private CharSequence updateBeginMessage;
	private CharSequence updateFinishedMessage;
	private CharSequence noUpdateMessage;
	private RemoteViews currentNotificationView;
	
	private Notification notification;
	private PendingIntent contentIntent;
	
	private static final int UPDATE_DATA_ID = 1;
	
	public NotificationSupervisor(Context context) {
		this.context = context;
		this.notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
		this.stringBuilder = new StringBuilder();
		this.updateBeginMessage = this.context.getText(R.string.dataUpdating);
		this.updateFinishedMessage = this.context.getText(R.string.dataUpdated);
		this.noUpdateMessage = this.context.getText(R.string.noDataUpdated);
	}

	/** 
	 * update existing notification
	 * @see cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener#OnStockDataUpdated(cz.tomas.StockAnalyze.Data.IStockDataProvider)
	 */
	@Override
	public void OnStockDataUpdated(IStockDataProvider sender, Map<String,DayData> dataMap) {
		showFinishNotification();
	}

	/** 
	 * create notification about data update, that is just proceeding
	 * @see cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener#OnStockDataUpdateBegin(cz.tomas.StockAnalyze.Data.IStockDataProvider)
	 */
	@Override
	public void OnStockDataUpdateBegin(IStockDataProvider sender) {
		showStartNotfication(sender.getAdviser().getMarket());

	}

	private void showStartNotfication(Market... markets) {
		final boolean enableNotif = this.context.getSharedPreferences(Utils.PREF_NAME, 0).getBoolean(Utils.PREF_UPDATE_NOTIF, true);
		
		if (markets == null || enableNotif == false)
			return;
		if (this.currentNotificationView == null)
			this.currentNotificationView = new RemoteViews(this.context.getPackageName(), R.layout.custom_update_notification_layout);
		this.currentNotificationView.setImageViewResource(R.id.notification_image, R.drawable.ic_stat_arrow);
		this.stringBuilder.setLength(0);
		this.stringBuilder.append(this.updateBeginMessage);
		this.stringBuilder.append(" from ");
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
//		this.currentNotificationView.setTextViewText(R.id.notification_text, this.stringBuilder.toString());
//		this.currentNotificationView.setTextViewText(R.id.notification_subtext, this.context.getText(R.string.app_name));
		
		this.notification = new Notification(R.drawable.ic_stat_arrow, this.updateBeginMessage, System.currentTimeMillis());
		//notification.defaults |= Notification.DEFAULT_SOUND;
		this.notification.flags |= Notification.FLAG_AUTO_CANCEL;
		//this.notification.contentView = this.currentNotificationView;
		
		// set intent to launch when the notification is tapped
		Intent notificationIntent = new Intent(this.context, StocksActivity.class);
		this.contentIntent = PendingIntent.getActivity(this.context, 0, notificationIntent, 0);
		//this.notification.contentIntent = contentIntent;
		this.notification.setLatestEventInfo(this.context, this.context.getText(R.string.app_name), this.stringBuilder.toString(), contentIntent);
		
		notificationManager.notify(UPDATE_DATA_ID, this.notification);
	}

	private void showFinishNotification() {
		if (this.currentNotificationView != null) {
			this.stringBuilder.setLength(0);
			this.stringBuilder.append(this.updateFinishedMessage);
			this.stringBuilder.append(": ");
			this.stringBuilder.append(FormattingUtils.formatStockDate(Calendar.getInstance()));
			

			if (this.context.getSharedPreferences(Utils.PREF_NAME, 0).getBoolean(Utils.PREF_PERMANENT_NOTIF, true)) {
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
	public void OnStockDataNoUpdate(IStockDataProvider sender) {
		showNoUpdateNotification();
	}

	@Override
	public void onUpdateBegin(Market... markets) {
		this.showStartNotfication(markets);
	}

	@Override
	public void onUpdateFinished(boolean succes) {
		if (succes) {
			this.showFinishNotification();
		} else {
			this.showNoUpdateNotification();
		}
	}

}
