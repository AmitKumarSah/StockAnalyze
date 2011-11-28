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

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Toast;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.charts.view.CompositeChartView;
import cz.tomas.StockAnalyze.ui.widgets.ActionBar;
import cz.tomas.StockAnalyze.ui.widgets.ActionBar.IActionBarListener;
import cz.tomas.StockAnalyze.ui.widgets.HomeBlockView;
import cz.tomas.StockAnalyze.ui.widgets.PickStockDialog;
import cz.tomas.StockAnalyze.ui.widgets.PickStockDialog.IStockDialogListener;
import cz.tomas.StockAnalyze.utils.Markets;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

public class HomeActivity extends ChartActivity implements OnClickListener, OnKeyListener, IActionBarListener {
	
	private SharedPreferences pref;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// this will set default values for first use
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		this.setContentView(R.layout.home_layout);
				
		this.chartView = (CompositeChartView) this.findViewById(R.id.stockChartView);
		this.chartView.setEnableTracking(false);
		this.pref = getSharedPreferences(Utils.PREF_NAME, 0);
		
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

		Thread thread = new Thread(chartRunnable);
		thread.start();
		//final TextView txtChartDescription = (TextView) this.findViewById(R.id.chartDescription);
//		this.setChartActivityListener(new IChartActivityListener() {
//			
//			@Override
//			public void onChartUpdateFinish() {
//				int id = DAY_COUNT_MAP.get(timePeriod);
//				if (txtChartDescription != null && stockItem != null) {
//					String text = String.format("%s (%s)", stockItem.getName(), getString(id));
//					txtChartDescription.setText(text);
//				}
//			}
//			
//			@Override
//			public void onChartUpdateBegin() {
//				if (txtChartDescription != null && stockItem != null) {
//					String text = String.format("%s %s", stockItem.getTicker(), getString(R.string.loading));
//					txtChartDescription.setText(text);
//				}
//			}
//		});

		ActionBar bar = (ActionBar) findViewById(R.id.homeActionBar);
		if (bar != null)
			bar.setActionBarListener(this);
		
		//this.registerForContextMenu(this.chartView);
	}
	
	private final Runnable chartRunnable = new Runnable() {
		public void run() {
			try {
				String ticker = pref.getString(Utils.PREF_HOME_CHART_TICKER, "PX");
				String marketId = pref.getString(Utils.PREF_HOME_CHART_MARKET_ID, Markets.CZ.getId());
				
				HomeActivity.this.stockItem = HomeActivity.this.dataManager.getStockItem(ticker, Markets.getMarket(marketId));
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
	
	@Override
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.app_menu, menu);
        return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// change chart in case something changed the time period
		final int period = this.prefs.getInt(Utils.PREF_CHART_TIME_PERIOD, DataManager.TIME_PERIOD_MONTH);
		if (period != this.timePeriod && this.stockItem != null) {
			this.updateTimePeriod(period, true);
		}
		this.setDescriptionVisibility(View.VISIBLE);
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
	    case R.id.menu_app_pick_stock:
			this.showDialog(DIALOG_PICK_STOCK);
			return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_PICK_STOCK) {
			final PickStockDialog dialog = new PickStockDialog(this, true);
			dialog.setListener(new IStockDialogListener() {
				
				@Override
				public void onStockSelected(StockItem item) {
					Editor ed = pref.edit();
					ed.putString(Utils.PREF_HOME_CHART_TICKER, item.getId());
					ed.commit();
					dialog.dismiss();
					stockItem = item;
					updateChart();
				}
			});
			return dialog;
		}
		return super.onCreateDialog(id);
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
					intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					startActivity(intent);
				}
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "failed to start activity", e);
				Toast.makeText(this, "Failed to start:\n" + (target == null ? "unkown" : target), Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onAction(int viewId) {
		if (viewId == R.id.actionHelpButton) {
			NavUtils.gotToAbout(this);
		} else if (viewId == R.id.actionChartButton) {
			this.showDialog(DIALOG_PICK_STOCK);
		}
	}
}
