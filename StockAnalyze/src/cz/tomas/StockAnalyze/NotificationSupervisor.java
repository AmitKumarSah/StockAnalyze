/**
 * 
 */
package cz.tomas.StockAnalyze;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import cz.tomas.StockAnalyze.Data.IStockDataProvider;
import cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener;

/**
 * @author tomas
 *
 */
public class NotificationSupervisor implements IStockDataListener {

	private Context context;
	private NotificationManager notificationManager;
	private StringBuilder stringBuilder;
	private CharSequence updateBeginMessage;
	
	private static final int UPDATE_BEGIN_ID = 1;
	
	public NotificationSupervisor(Context context) {
		this.context = context;
		this.notificationManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
		this.stringBuilder = new StringBuilder();
		this.updateBeginMessage = this.context.getText(R.string.dataUpdating);
	}

	/* (non-Javadoc)
	 * @see cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener#OnStockDataUpdated(cz.tomas.StockAnalyze.Data.IStockDataProvider)
	 */
	@Override
	public void OnStockDataUpdated(IStockDataProvider sender) {
		// TODO Auto-generated method stub

	}

	/* 
	 * create notification about data update, that is just proceeding
	 * @see cz.tomas.StockAnalyze.Data.Interfaces.IStockDataListener#OnStockDataUpdateBegin(cz.tomas.StockAnalyze.Data.IStockDataProvider)
	 */
	@Override
	public void OnStockDataUpdateBegin(IStockDataProvider sender) {
		if (sender == null)
			return;
		RemoteViews contentView = new RemoteViews(this.context.getPackageName(), R.layout.custom_update_notification_layout);
		contentView.setImageViewResource(R.id.notification_image, R.drawable.ic_launcher);
		this.stringBuilder.setLength(0);
		this.stringBuilder.append(this.updateBeginMessage);
		this.stringBuilder.append(" from ").append(sender.getDescriptiveName());
		
		contentView.setTextViewText(R.id.notification_text, this.stringBuilder.toString());
		contentView.setTextViewText(R.id.notification_subtext, this.context.getText(R.string.app_name));
		
		Notification notification = new Notification(R.drawable.ic_launcher, this.updateBeginMessage, System.currentTimeMillis());
		//notification.defaults |= Notification.DEFAULT_SOUND;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.contentView = contentView;
		
		// set intent to launch when the notification is tapped
		Intent notificationIntent = new Intent(this.context, StockListActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this.context, 0, notificationIntent, 0);
		notification.contentIntent = contentIntent;
		
		notificationManager.notify(UPDATE_BEGIN_ID, notification);

	}

}
