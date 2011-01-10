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
							if (intent.hasExtra("stock_id") && intent.hasExtra("market_id")) {
								String id = intent.getExtras().getString("stock_id");
								Market market = (Market) intent.getExtras().getSerializable("market_id");
								
								updateCurrentStock(id, market);
							}
							else
								showWarning();
						}
					} catch (Exception e) {
						e.printStackTrace();
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
			if (intent.hasExtra("stock_id") && intent.hasExtra("market_id")) {
				String id = intent.getExtras().getString("stock_id");
				Market market = (Market) intent.getExtras().getSerializable("market_id");
				
				try {
					updateCurrentStock(id, market);
				} catch (Exception e) {
					e.printStackTrace();
					Toast toast = Toast.makeText(StockDetailActivity.this, R.string.InvalidData, Toast.LENGTH_LONG);
					if (e.getMessage() != null) {
						Log.d("StockDetailActivity", e.getMessage());
						toast.setText(getString(R.string.InvalidData) + ": " + e.getMessage());
					}
					toast.show();
				}
			}
			else
				showWarning();
		}
	}

	private void updateCurrentStock(final String stockId, Market market) throws NullPointerException, IOException {
		if (stockId == null || stockId.length() == 0)
			throw new NullPointerException("StockID must be defined");
		if (market == null)
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
		StockItem stockItem = manager.getStockItem(stockId, market);
		DayData data = manager.getLastValue(stockItem);
		
		NumberFormat priceFormat = FormattingUtils.getPriceFormat(stockItem.getMarket().getCurrency());
		NumberFormat percentFormat = FormattingUtils.getPercentFormat();
		NumberFormat volumeFormat = FormattingUtils.getVolumeFormat();

		if (stockItem == null)
			throw new NullPointerException("No such stock has been found!");
		if (data == null)
			throw new NullPointerException("Day data is null!");
		
		if (txtHeader != null)
			txtHeader.setText(stockItem.getTicker() + " - " + stockId);
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
