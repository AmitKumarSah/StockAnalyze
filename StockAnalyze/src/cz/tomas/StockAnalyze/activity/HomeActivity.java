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
import cz.tomas.StockAnalyze.charts.view.CompositeChartView;
import cz.tomas.StockAnalyze.ui.widgets.ActionBar;
import cz.tomas.StockAnalyze.ui.widgets.ActionBar.IActionBarListener;
import cz.tomas.StockAnalyze.ui.widgets.HomeBlockView;
import cz.tomas.StockAnalyze.utils.DownloadService;
import cz.tomas.StockAnalyze.utils.Markets;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

public class HomeActivity extends ChartActivity implements OnClickListener, OnKeyListener, IActionBarListener {

	private DataManager dataManager;
	@SuppressWarnings("unused")
	private static Bitmap chartBitmap;
	@SuppressWarnings("unused")
	private static long chartLastUpdate;
	@SuppressWarnings("unused")
	private static long chartUpdateInterval = 1000 * 60 * 10;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.home_layout);
		
		this.chartView = (CompositeChartView) this.findViewById(R.id.stockChartView);
		this.dataManager = DataManager.getInstance(this);
		
		View[] blockViews = new View[4];
		blockViews[0] = this.findViewById(R.id.homeBlockIndeces);
		blockViews[1] = this.findViewById(R.id.homeBlockNews);
		blockViews[2] = this.findViewById(R.id.homeBlockPortfolio);
		blockViews[3] = this.findViewById(R.id.homeBlockStockList);
		
		for (View view : blockViews) {
			if (view != null) {
				view.setOnClickListener(this);
				view.setOnKeyListener(this);
			}
		}
		final Runnable runnable = new Runnable() {
			public void run() {
				try {
					HomeActivity.this.stockItem = HomeActivity.this.dataManager.getStockItem("PX", Markets.CZ);
				} catch (Exception e) {
					Log.e(Utils.LOG_TAG, "filed to get stock item for home screen chart", e);
				}
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						updateChart();
					}
				});
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
		ActionBar bar = (ActionBar) findViewById(R.id.homeActionBar);
		if (bar != null)
			bar.setActionBarListener(this);
		
		this.registerForContextMenu(this.chartView);
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

	@Override
	public void onAction(int viewId) {
		if (viewId == R.id.actionHelpButton)
			NavUtils.gotToAbout(this);
	}
}
