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

import org.apache.http.HttpVersion;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Portfolio.Portfolio;
import cz.tomas.StockAnalyze.activity.ChartActivity;
import cz.tomas.StockAnalyze.utils.Utils;
import android.util.Log;

public class Application extends android.app.Application {

	public static final String UPDATE_SCHEDULER_SERVICE = "cz.tomas.StockAnalyze.Data.UpdateScheduler"; 
	public static final String DATA_MANAGER_SERVICE = "cz.tomas.StockAnalyze.Data.DataManager";
	public static final String PORTFOLIO_SERVICE = "cz.tomas.StockAnalyze.Data.Portfolio";
	public static final String HTTP_CLIENT_SERVICE = "httpClient";
	
	private UpdateScheduler scheduler;
	private DataManager dataManager;
	private Portfolio portfolio;
	
	private static DefaultHttpClient sDefaultHttpClient;
	
	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		Log.i(Utils.LOG_TAG, "STARTING THE APPLICATION");
		this.scheduler = new UpdateScheduler(this);
		this.dataManager = DataManager.getInstance(this);
		NotificationSupervisor supervisor = new NotificationSupervisor(this);
		this.scheduler.addListener(supervisor);

		// do immediate update and schedule next one
		try {
			if (! this.scheduler.isSchedulerRunning()) {
				this.scheduler.updateImmediatly();
				this.scheduler.scheduleNextIntraDayUpdate();
				//this.scheduler.scheduleNextDayUpdate();
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
		if (UPDATE_SCHEDULER_SERVICE.equals(name)) {
			return this.scheduler;
		} else if (DATA_MANAGER_SERVICE.equals(name)) {
			return this.dataManager;
		} else if (PORTFOLIO_SERVICE.equals(name)) {
			if (this.portfolio == null) {
				this.portfolio = new Portfolio(this);
			}
			return portfolio;
		} else if (HTTP_CLIENT_SERVICE.equals(name)) {
			if (sDefaultHttpClient == null) {
				HttpParams params = new BasicHttpParams();
				HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
				HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
				HttpProtocolParams.setUseExpectContinue(params, false);
				ConnManagerParams.setMaxTotalConnections(params, 10);
				HttpConnectionParams.setConnectionTimeout(params, 10 * 1000);
				HttpConnectionParams.setSoTimeout(params, 10 * 1000);

				SchemeRegistry schReg = new SchemeRegistry();
				schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
				schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
				
				sDefaultHttpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, schReg), params);
			}
			
			return sDefaultHttpClient;
		}
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
