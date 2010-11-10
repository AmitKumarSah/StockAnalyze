/**
 * 
 */
package cz.tomas.StockAnalyze;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Formatter;
import java.util.Locale;

import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import android.app.Activity;
import android.app.TabActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;

/**
 * @author tomas
 *
 */
public class StockDetailActivity extends Activity {

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
							if (StockDetailActivity.this.getParent().getIntent().hasExtra("stock_id")) {
								String id = StockDetailActivity.this
										.getParent().getIntent()
										.getStringExtra("stock_id");
								updateCurrentStock(id);
							}
							else
								showWarning();
						}
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(StockDetailActivity.this, R.string.InvalidData, Toast.LENGTH_LONG).show();
						if (e.getMessage() != null)
							Log.d("StockDetailActivity", e.getMessage());
					}
				}
			});
		}
	}

	private void updateCurrentStock(final String stockId) throws NullPointerException, IOException {
		
		TextView txtHeader = (TextView) this.findViewById(R.id.txtDetailHeader);
		TextView txtDate = (TextView) this.findViewById(R.id.txtDetailDate);
		TextView txtName = (TextView) this.findViewById(R.id.txtDetailName);
		TextView txtPrice = (TextView) this.findViewById(R.id.txtDetailClosingPrice);
		TextView txtVolume = (TextView) this.findViewById(R.id.txtDetailVolume);
		TextView txtMax = (TextView) this.findViewById(R.id.txtDetailMax);
		TextView txtMin = (TextView) this.findViewById(R.id.txtDetailMin);
		
		final DataManager manager = DataManager.getInstance(this);
		
		if (txtPrice != null) {
			txtPrice.setText(R.string.loading);
		}
		StockItem stockItem = manager.getStockItem(stockId);
		DayData data = manager.getLastValue(stockItem.getTicker());
		
		NumberFormat priceFormat = DecimalFormat.getCurrencyInstance();
		// TODO set currency according to stock item
		priceFormat.setCurrency(Currency.getInstance("CZK"));
		NumberFormat percentFormat = DecimalFormat.getNumberInstance();
		NumberFormat volumeFormat = DecimalFormat.getNumberInstance();
		volumeFormat.setGroupingUsed(true);

		if (stockItem == null)
			throw new NullPointerException("No such stock has been found!");
		if (data == null)
			throw new NullPointerException("Day data is null!");
		
		if (txtHeader != null)
			txtHeader.setText(stockItem.getTicker() + " - " + stockId);
		if (txtDate != null) {
			DateFormat formatter = DateFormat.getDateInstance(DateFormat.LONG);
			txtDate.setText(formatter.format(data.getDate()));
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
			String strChange = percentFormat.format(data.getChange());
			
			txtPrice.setText(String.format("%s (%s%%)", strPrice, strChange));
			if (data.getChange() > 0f)
        		txtPrice.setTextColor(Color.GREEN);
			else if (data.getChange() < 0f)
            	txtPrice.setTextColor(Color.RED);
		}
	}

	private void showWarning() {
		TextView txtName = (TextView) this.findViewById(R.id.txtDetailName);
		
		if (txtName != null)
			txtName.setText(R.string.NoStockSelected);
	}
}
