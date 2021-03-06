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
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.crittercism.NewFeedbackSpringboardActivity;
import com.crittercism.app.Crittercism;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.UpdateScheduler;
import cz.tomas.StockAnalyze.charts.view.CompositeChartView;
import cz.tomas.StockAnalyze.ui.widgets.HomeBlockView;
import cz.tomas.StockAnalyze.ui.widgets.PickStockDialog;
import cz.tomas.StockAnalyze.ui.widgets.PickStockDialog.IStockDialogListener;
import cz.tomas.StockAnalyze.utils.Consts;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

public class HomeActivity extends ChartActivity implements OnClickListener {

	private SharedPreferences pref;

	private TextView txtChartLabel;

	private static boolean isDataUpdated;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Crittercism.init(getApplicationContext(), Consts.CRITTER_ID);
		if (Utils.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			StrictMode.enableDefaults();
		}
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
			}
		}

		Thread thread = new Thread(chartRunnable);
		thread.start();

		this.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		if (! isDataUpdated) {
			// user just launched the application, let's make an update
			UpdateScheduler scheduler = (UpdateScheduler) getApplicationContext().getSystemService(Application.UPDATE_SCHEDULER_SERVICE);
			scheduler.updateImmediately();
			isDataUpdated = true;
		}

		this.txtChartLabel = (TextView) this.findViewById(R.id.chartDescription);
		if (this.txtChartLabel != null) {
			this.txtChartLabel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showDialog(DIALOG_PICK_STOCK);
				}
			});
		}
	}
	
	private final Runnable chartRunnable = new Runnable() {
		public void run() {
			try {
				String ticker = pref.getString(Utils.PREF_HOME_CHART_TICKER, null);

				if (!TextUtils.isEmpty(ticker)) {
					HomeActivity.this.stockItem = HomeActivity.this.dataManager.getStockItem(ticker);
				}
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "failed to get stock item for home screen chart", e);
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
	    MenuInflater inflater = getSupportMenuInflater();
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
	    case R.id.menu_app_feedback:
		    Intent i = new Intent(this, NewFeedbackSpringboardActivity.class);
		    startActivity(i);
		    return true;
	    case R.id.menu_app_diag:
		    Intent intent = new Intent(this, DiagActivity.class);
		    startActivity(intent);
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
	protected void onChartUpdateFinish() {
		super.onChartUpdateFinish();

		if (stockItem != null) {
			this.txtChartLabel.setText(stockItem.getName());
		}
	}

	@Override
	protected void onChartUpdateBegin() {
		super.onChartUpdateBegin();
		if (stockItem != null) {
			this.txtChartLabel.setText(stockItem.getName());
		}
	}

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
				Toast.makeText(this, "Failed to start:\n" + (target == null ? "unknown" : target), Toast.LENGTH_SHORT).show();
			}
		}
	}
}
