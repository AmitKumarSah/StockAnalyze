/**
 * 
 */
package cz.tomas.StockAnalyze;

import java.io.IOException;

import cz.tomas.StockAnalyze.Data.DataManager;
import android.app.Activity;
import android.app.TabActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

	private void updateCurrentStock(String ticker) throws NullPointerException, IOException {
		
		TextView txtView = (TextView) this.findViewById(R.id.txtHeader);
		txtView.setText(ticker);
		
		DataManager manager = new DataManager(this);
		
		TextView txtPrice = (TextView) this.findViewById(R.id.txtClosingPrice);
		txtPrice.setText(R.string.loading);
		
		StockDetailUpdateThread thread = new StockDetailUpdateThread(this.updateHandler, manager, ticker);
		thread.setName("StockDetailUpdate");
		thread.start();
	}
	
	// Define the Handler that receives messages from the thread
    final Handler updateHandler = new Handler() {
        public void handleMessage(Message msg) {
        	boolean result = msg.getData().getBoolean(StockListActivity.MSG_UPDATE_RESULT);
        	
        	
        	if (result) {
            	TextView txt = (TextView) StockDetailActivity.this.findViewById(R.id.txtClosingPrice);
            	String price = msg.getData().getString("price");
            	txt.setText(price);
        	}

            else {
        		Toast.makeText(StockDetailActivity.this, R.string.failed_price_update, Toast.LENGTH_LONG).show();
            }
            
        }
    };
	
	private class StockDetailUpdateThread extends Thread {
		Handler handler;
		DataManager dataManager;
		String ticker;
		
		public StockDetailUpdateThread(Handler handler, DataManager dataManager, String ticker) {
			this.handler = handler;
			this.dataManager = dataManager;
			this.ticker = ticker;
		}
		
		@Override
		public void run()
		{			
			Message msg = new Message();
			Bundle bundle = new Bundle();
			
			try {
				float value = this.dataManager.getLastValue(this.ticker);
				bundle.putString("price", String.valueOf(value));
				bundle.putBoolean("result", true);
			} catch (Exception e) {
				bundle.putBoolean("result", false);
			}
			
			msg.setData(bundle);
			if (this.handler != null) {
				handler.sendMessage(msg);
			}
		}
	}
}
