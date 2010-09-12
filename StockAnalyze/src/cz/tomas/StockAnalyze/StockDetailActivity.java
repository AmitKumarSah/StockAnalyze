/**
 * 
 */
package cz.tomas.StockAnalyze;

import android.app.Activity;
import android.app.TabActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
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
							String ticker = StockDetailActivity.this.getParent().getIntent().getStringExtra("ticker");
							updateCurrentStock(ticker);
						}
					} catch (Exception e) {
						e.printStackTrace();
						Log.d("StockDetailActivity", e.getMessage());
					}
				}
			});
		}
	}

	private void updateCurrentStock(String ticker) {
		TextView txtView = (TextView) this.findViewById(R.id.txtHeader);
		txtView.setText(ticker);
	}
}
