/**
 * 
 */
package cz.tomas.StockAnalyze;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * @author tomas
 *
 */
public final class StockDetailActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.stock_detail);
		
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

	/**
	 * read data from intent
	 * @param intent
	 * @throws NullPointerException
	 * @throws IOException
	 */
	private void readData(Intent intent) throws NullPointerException,
			IOException {
		//if (intent.hasExtra("stock_id") && intent.hasExtra("market_id")) {
		if (intent.hasExtra(NavUtils.STOCK_ITEM_OBJECT)) {
			StockItem stockItem = intent.getExtras().getParcelable(NavUtils.STOCK_ITEM_OBJECT);
			DayData data = intent.getExtras().getParcelable(NavUtils.DAY_DATA_OBJECT);
			//Market market = (Market) intent.getExtras().getSerializable("market_id");
			
			updateCurrentStock(stockItem,data);
		}
		else
			showWarning();
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
}
