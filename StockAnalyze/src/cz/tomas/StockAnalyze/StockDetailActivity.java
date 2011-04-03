/**
 * 
 */
package cz.tomas.StockAnalyze;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.charts.view.CompositeChartView;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * @author tomas
 *
 */
public final class StockDetailActivity extends Activity {

	private StockItem stockItem;
	private DataManager dataManager;
	private int chartDayCount = 5;
	private CompositeChartView chartView;
	private DrawChartTask chartTask;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.stock_detail);
		this.dataManager = DataManager.getInstance(this);
		this.chartView = (CompositeChartView) findViewById(R.id.stockChartView);
		this.registerForContextMenu(this.chartView);
		
		if (this.getParent() instanceof TabActivity) {
			((TabActivity) this.getParent()).getTabHost().setOnTabChangedListener(new OnTabChangeListener() {
				
				@Override
				public void onTabChanged(String tabId) {
					try {
						if (tabId.equals("StockDetail")) {
							Intent intent = StockDetailActivity.this.getParent().getIntent();
							readData(intent);
						}
					} catch (Exception e) {
						Log.e(Utils.LOG_TAG, "failed to get data from intent", e);
						Toast toast = Toast.makeText(StockDetailActivity.this, R.string.InvalidData, Toast.LENGTH_LONG);
						if (e.getMessage() != null) {
							Log.d("StockDetailActivity", e.getMessage());
							toast.setText(getString(R.string.InvalidData) + ": " + e.getMessage());
						}
						toast.show();
					}
				}
			});
		}
		else {
			Intent intent = this.getIntent();
			try {
				readData(intent);
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "failed to get data from intent", e);
				Toast toast = Toast.makeText(StockDetailActivity.this, R.string.InvalidData, Toast.LENGTH_LONG);
				if (e.getMessage() != null) {
					Log.d("StockDetailActivity", e.getMessage());
					toast.setText(getString(R.string.InvalidData) + ": " + e.getMessage());
				}
				toast.show();
			}
		}
	}
		

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		if (this.chartTask != null && this.chartTask.getStatus() != Status.FINISHED) {
			Log.w(Utils.LOG_TAG, "canceling chart update task!");
			this.chartTask.cancel(true);
		}
		super.onPause();
	}


	/* 
	 * create context menu for chart
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.chart_context_menu, menu);
	}



	/* 
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int dayCount = 0;
		switch (item.getItemId()) {
		case R.id.chart10days:
			dayCount = 10;
			break;
		case R.id.chart3months:
			dayCount = 91;
			break;
		case R.id.chart5days:
			dayCount = 5;
			break;
		case R.id.chart6months:
			dayCount =  182;
			break;
		case R.id.chartMonth:
			dayCount = 30;
			break;
		case R.id.chartYear:
			dayCount = 365;
			break;
		default:
			break;
		}
		if (dayCount != 0) {
			this.chartDayCount = dayCount;
			this.updateChart();
			return true;
		} else
			return super.onContextItemSelected(item);
	}



	/**
	 * read data from input intent
	 * @param intent
	 * @throws NullPointerException
	 * @throws IOException
	 */
	private void readData(Intent intent) throws NullPointerException,
			IOException {
		if (intent.hasExtra(NavUtils.STOCK_ITEM_OBJECT)) {
			this.stockItem = intent.getExtras().getParcelable(NavUtils.STOCK_ITEM_OBJECT);
			DayData data = intent.getExtras().getParcelable(NavUtils.DAY_DATA_OBJECT);
			//Market market = (Market) intent.getExtras().getSerializable("market_id");
			
			updateChart();
			updateCurrentStock(stockItem,data);
		}
		else
			showWarning();
	}


	/**
	 * @param stockItem
	 */
	private void updateChart() {
		this.chartTask = new DrawChartTask();
		chartTask.execute(this.stockItem);
	}

	private void updateCurrentStock(final StockItem stockItem, DayData data) throws NullPointerException, IOException {
		if (stockItem == null)
			throw new NullPointerException("stockItem must be defined");
		if (stockItem.getMarket() == null)
			throw new NullPointerException("market must be defined");
		
		TextView txtHeader = (TextView) this.findViewById(R.id.txtDetailHeader);
		TextView txtDate = (TextView) this.findViewById(R.id.txtDetailDate);
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
		if (data == null) {
			Log.w(Utils.LOG_TAG, "data incoming to detail don't contain DayData. Loading them from db...");
			data = manager.getLastValue(stockItem);
		}
		
		NumberFormat priceFormat = FormattingUtils.getPriceFormat(stockItem.getMarket().getCurrency());
		NumberFormat percentFormat = FormattingUtils.getPercentFormat();
		NumberFormat volumeFormat = FormattingUtils.getVolumeFormat();

		if (data == null)
			throw new NullPointerException("Day data is null!");
		
		if (txtHeader != null)
			txtHeader.setText(stockItem.getTicker() + " - " + stockItem.getId());
		if (txtDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(data.getLastUpdate());
			
			txtDate.setText(FormattingUtils.formatStockDate(cal));
		}
		if (txtVolume != null) {
			String strVolume = priceFormat.format(data.getVolume());
			txtVolume.setText(strVolume);
		}
		if (txtMax != null)
			txtMax.setText(String.valueOf(data.getYearMaximum()));
		if (txtMin != null)
			txtMin.setText(String.valueOf(data.getYearMinimum()));
		if (txtName != null)
			txtName.setText(stockItem.getName());
		if (txtPrice != null) {
			String strPrice = priceFormat.format(data.getPrice());
			txtPrice.setText(strPrice);
			//txtPrice.setText(String.format("%s (%s%%)", strPrice, strChange));
		}
		if (txtChange != null) {
			String strChange = percentFormat.format(data.getChange());
			String strAbsChange = percentFormat.format(data.getAbsChange());
			txtChange.setText(String.format("%s (%s%%)", strAbsChange, strChange));
			
			if (data.getChange() > 0f)
				txtChange.setTextColor(Color.GREEN);
			else if (data.getChange() < 0f)
				txtChange.setTextColor(Color.RED);
		}
	}

	private void showWarning() {
		TextView txtName = (TextView) this.findViewById(R.id.txtDetailName);
		
		if (txtName != null)
			txtName.setText(R.string.NoStockSelected);
	}
	
	final class DrawChartTask extends AsyncTask<StockItem, Integer, Void> {
		
		/* 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			
		}

		@Override
		protected Void doInBackground(StockItem... params) {
			if (params.length == 0)
				return null;
			StockItem stockItem = params[0];
			DayData[] dataSet = null;
			try {
				dataSet = dataManager.getDayDataSet(stockItem, Calendar.getInstance(), chartDayCount, true);
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "failed to get data", e);
			}
			if (dataSet != null) {
				float[] dataPoints = new float[dataSet.length];
				Date[] xAxisPoints = new Date[dataSet.length];
				float max = 0;
				float min = Float.MAX_VALUE;
				for (int i = 0; i < dataSet.length; i++) {
					float price = 0;
					Date time = null;
					if (dataSet[i] != null) {
						price = dataSet[i].getPrice();
						time = dataSet[i].getDate();
					} else {
						Log.w(Utils.LOG_TAG, "day data with index " + i + " are not available");
						if (i > 0 && dataSet[i-1] != null) {
							// try to get previous one
							price = dataSet[i-1].getPrice();
							time = dataSet[i-1].getDate();
						}
					}
					dataPoints[i] = price;
					xAxisPoints[i] = time;
					
					if (price > max)
						max = price;
					if (price < min)
						min = price;
				}

				chartView.setData(dataPoints, max, min);
				chartView.setAxisX(xAxisPoints);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			
		}
		
	}
}
