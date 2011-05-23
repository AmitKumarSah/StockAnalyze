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
package cz.tomas.StockAnalyze.activity;

import java.io.IOException;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ImageView;
import android.widget.Toast;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.MarketFactory;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.R.id;
import cz.tomas.StockAnalyze.R.layout;
import cz.tomas.StockAnalyze.R.menu;
import cz.tomas.StockAnalyze.charts.view.ChartView;
import cz.tomas.StockAnalyze.ui.widgets.HomeBlockView;
import cz.tomas.StockAnalyze.utils.DownloadService;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

public class HomeActivity extends Activity implements OnClickListener, OnKeyListener {

	private DataManager dataManager;
	private static Bitmap chartBitmap;
	private static long chartLastUpdate;
	private static long chartUpdateInterval = 1000 * 60 * 10;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.home_layout);
		
		this.dataManager = DataManager.getInstance(this);
		
		View[] blockViews = new View[4];
		blockViews[0] = this.findViewById(R.id.homeBlockCurrencies);
		blockViews[1] = this.findViewById(R.id.homeBlockNews);
		blockViews[2] = this.findViewById(R.id.homeBlockPortfolio);
		blockViews[3] = this.findViewById(R.id.homeBlockStockList);
		
		for (View view : blockViews) {
			if (view != null) {
				view.setOnClickListener(this);
				view.setOnKeyListener(this);
			}
		}
//		DrawChartTask task = new DrawChartTask();
//		task.execute(null);
		//Debug.startMethodTracing();
		// if chart bitmap is null or too old, refresh it
		if (this.chartBitmap == null || (System.currentTimeMillis() - this.chartLastUpdate) > this.chartUpdateInterval) {
			ChartUpdateTask task = new ChartUpdateTask();
			task.execute((Void[])null);
		}
		else {
			ImageView chart = (ImageView) findViewById(R.id.home_chart);
			chart.setImageBitmap(this.chartBitmap);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.app_menu, menu);
	    return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_app_settings:
	    	NavUtils.goToSettings(this);
	        return true;
	    case R.id.menu_app_about:
	    	NavUtils.gotToAbout(this);
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		//Debug.stopMethodTracing();
	}

	@Override
	public void onClick(View v) {
		startChildActivity(v);
	}


	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			startChildActivity(v);
			return true;
		}
		else
			return false;
	}
	
	/**
	 * @param v
	 */
	private void startChildActivity(View v) {
		String target = null;
		if (v instanceof HomeBlockView) {
			try {
				target = ((HomeBlockView) v).getTarget();
				
				if (target != null) {
					Intent intent = new Intent();
					intent.setClassName(this, target);
					startActivity(intent);
				}
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "failed to start activity", e);
				Toast.makeText(this, "Failed to start:\n" + (target == null ? "unkown" : target), Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	final class DrawChartTask extends AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			DayData[] dataSet = null;
			try {
				StockItem item = dataManager.getStockItems(MarketFactory.getCzechMarket()).get("PX");
				dataSet = dataManager.getDayDataSet(item, Calendar.getInstance(), 10, false);
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "failed to get data", e);
			}
			if (dataSet != null) {
				float[] dataPoints = new float[dataSet.length];
				float max = 0;
				float min = Float.MAX_VALUE;
				for (int i = 0; i < dataSet.length; i++) {
					float price = dataSet[i].getPrice();
					dataPoints[i] = price;
					
					if (price > max)
						max = price;
					if (price < min)
						min = price;
				}
				ChartView chart = (ChartView) findViewById(R.id.homeChartView);
				chart.setData(dataPoints, max, min);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			
		}
		
	}

	final class ChartUpdateTask extends AsyncTask<Void, Integer, Bitmap> {

		@Override
		protected Bitmap doInBackground(Void... params) {
			String downloadUrl = "http://www.pse.cz/generated/a_indexy/X1_R.GIF";
			byte[] chartArray = null;
			Bitmap bmp = null;
			try {
				chartArray = DownloadService.GetInstance().DownloadFromUrl(downloadUrl, false);
				
				bmp = BitmapFactory.decodeByteArray(chartArray, 0, chartArray.length);

			} catch (IOException e) {
				e.printStackTrace();
			}
			return bmp;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			ImageView chart = (ImageView) findViewById(R.id.home_chart);
			chart.setImageBitmap(result);
			chartBitmap = result;
			chartLastUpdate = System.currentTimeMillis();
		}
		
	}
}
