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
package cz.tomas.StockAnalyze.activity;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.charts.view.CompositeChartView;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * activity with detail information of stock or index
 * @author tomas
 *
 */
public final class StockDetailActivity extends ChartActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.stock_detail);
		this.chartView = (CompositeChartView) findViewById(R.id.stockChartView);
		if (chartView != null) {
			this.chartView.setEnableTracking(false);
			this.chartView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					goToChartDetail();
				}
			});
		} else {
			Log.w(Utils.LOG_TAG, "Failed to initialize chart view");
		}
		
		final Intent intent = this.getIntent();
		try {
			if (readData(intent)) {
				final boolean index = this.stockItem.isIndex();
				final int logo = index ? R.drawable.ic_up_indeces
						: R.drawable.ic_up_list;
				final int textId = index ? R.string.activityIndexDetail
						: R.string.activityStockDetail;
				this.setTitle(textId);
				this.getActionBarHelper().setLogo(logo);
				this.fill();
			} else {
				this.showWarning();
			}
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to get data from intent", e);
			Toast toast = Toast.makeText(StockDetailActivity.this,
					R.string.InvalidData, Toast.LENGTH_LONG);
			toast.show();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.stock_detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.menu_stock_detail_add) {
			NavUtils.goToAddToPortfolio(this, this.stockItem, this.dayData);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void fill() throws NullPointerException, IOException {
		this.updateChart();
		this.updateCurrentStock();
	}
	
	private void goToChartDetail() {
		Intent intent = new Intent(StockDetailActivity.this,
				StockChartActivity.class);
		intent.putExtras(getIntent());
		intent.putExtra(EXTRA_CHART_DAY_COUNT, this.timePeriod);
		startActivity(intent);
	}

	private void updateCurrentStock() throws NullPointerException, IOException {
		if (this.stockItem == null) {
			throw new NullPointerException("stockItem must be defined");
		} else if (this.stockItem.getMarket() == null) {
			throw new NullPointerException("market must be defined");
		}
		
		TextView txtHeader = (TextView) this.findViewById(R.id.txtDetailHeader);
		TextView txtName = (TextView) this.findViewById(R.id.txtDetailName);
		TextView txtPrice = (TextView) this.findViewById(R.id.txtDetailClosingPrice);
		TextView txtChange = (TextView) this.findViewById(R.id.txtDetailChange);
		TextView txtVolume = (TextView) this.findViewById(R.id.txtDetailVolume);
		TextView txtMax = (TextView) this.findViewById(R.id.txtDetailMax);
		TextView txtMin = (TextView) this.findViewById(R.id.txtDetailMin);
		
		final DataManager manager = DataManager.getInstance(this);
		
		if (txtPrice != null) {
			txtPrice.setText(R.string.loading);
		}
		if (this.dayData == null) {
			Log.w(Utils.LOG_TAG, "data incoming to detail don't contain DayData. Loading them from db...");
			this.dayData = manager.getLastValue(stockItem);
		}
		
		final NumberFormat priceFormat = FormattingUtils.getPriceFormat(stockItem.getMarket().getCurrency());
		final NumberFormat percentFormat = FormattingUtils.getPercentFormat();

		if (this.dayData == null)
			throw new NullPointerException("Day data is null!");
		
		if (txtHeader != null) {
			Calendar cal = new GregorianCalendar();
			cal.setTimeInMillis(this.dayData.getLastUpdate());
			String time = FormattingUtils.formatStockDate(cal);
			time += " " + cal.getTimeZone().getDisplayName(true, TimeZone.SHORT);
			
			txtHeader.setText(String.format("%s - %s", time, stockItem.getTicker() /*, stockItem.getId() */));
			txtHeader.setSelected(true);
		}
		if (txtVolume != null) {
			String strVolume = String.valueOf((int) dayData.getVolume()) + getString(R.string.pieces);
			txtVolume.setText(strVolume);
		}
		if (txtMax != null)
			txtMax.setText(String.valueOf(this.dayData.getYearMaximum()));
		if (txtMin != null)
			txtMin.setText(String.valueOf(this.dayData.getYearMinimum()));
		if (txtName != null)
			txtName.setText(stockItem.getName());
		if (txtPrice != null) {
			String strPrice = priceFormat.format(this.dayData.getPrice());
			txtPrice.setText(strPrice);
		}
		if (txtChange != null) {
			String strChange = percentFormat.format(this.dayData.getChange());
			String strAbsChange = percentFormat.format(this.dayData.getAbsChange());
			txtChange.setText(String.format("%s (%s%%)", strAbsChange, strChange));
			
			if (this.dayData.getChange() > 0f) {
				txtChange.setTextColor(getResources().getColor(R.color.money_item_green));
			} else if (this.dayData.getChange() < 0f) {
				txtChange.setTextColor(getResources().getColor(R.color.money_item_red));
			}
		}
	}

	private void showWarning() {
		TextView txtName = (TextView) this.findViewById(R.id.txtDetailName);
		
		if (txtName != null)
			txtName.setText(R.string.NoStockSelected);
	}
	
	@Override
	protected void onNavigateUp() {
		//NavUtils.goUp(this, StocksActivity.class);
		finish();
	}
}
