/**
 * 
 */
package cz.tomas.StockAnalyze;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
							//String ticker = StockDetailActivity.this.getParent().getIntent().getStringExtra("ticker");
							String id = StockDetailActivity.this.getParent().getIntent().getStringExtra("stock_id");
							updateCurrentStock(id);
						}
					} catch (Exception e) {
						e.printStackTrace();
						Log.d("StockDetailActivity", e.getMessage());
					}
				}
			});
		}
	}

	private void updateCurrentStock(final String stockId) throws NullPointerException, IOException {
		
		TextView txtHeader = (TextView) this.findViewById(R.id.txtDetailHeader);
		TextView txtName = (TextView) this.findViewById(R.id.txtDetailName);
		TextView txtPrice = (TextView) this.findViewById(R.id.txtDetailClosingPrice);
		TextView txtVolume = (TextView) this.findViewById(R.id.txtDetailVolume);
		TextView txtMax = (TextView) this.findViewById(R.id.txtDetailMax);
		TextView txtMin = (TextView) this.findViewById(R.id.txtDetailMin);
		
		final DataManager manager = new DataManager(this);
		
		if (txtPrice != null) {
			txtPrice.setText(R.string.loading);
			txtPrice.setTextColor(Color.WHITE);
		}
		final StockItem stockItem = manager.getStockItem(stockId);
		final DayData data = manager.getLastValue(stockItem.getTicker());
		
		if (data == null)
			throw new NullPointerException("Day data is null!");
		if (stockItem == null)
			throw new NullPointerException("No such stock has been found!");
		
		if (txtHeader != null)
			txtHeader.setText(stockItem.getTicker() + " - " + stockId);
		if (txtVolume != null)
			txtVolume.setText(String.format("%s", data.getVolume()));
		if (txtMax != null)
			txtMax.setText(String.valueOf(data.getYearMaximum()));
		if (txtMin != null)
			txtMin.setText(String.valueOf(data.getYearMinimum()));
		if (txtName != null)
			txtName.setText(stockItem.getName());
		if (txtPrice != null) {
			txtPrice.setText(String.format("%s (%s)", String.valueOf(data.getPrice()), String.valueOf(data.getChange())));
			if (data.getChange() > 0f)
        		txtPrice.setTextColor(Color.GREEN);
			else if (data.getChange() < 0f)
            	txtPrice.setTextColor(Color.RED);
        	else
        		txtPrice.setTextColor(Color.WHITE);
		}
	}
}
