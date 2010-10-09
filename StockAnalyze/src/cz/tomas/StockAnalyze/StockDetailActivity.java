/**
 * 
 */
package cz.tomas.StockAnalyze;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import android.app.Activity;
import android.app.TabActivity;
import android.graphics.Color;
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
		txtPrice.setTextColor(Color.WHITE);
		
		StockDetailUpdateThread thread = new StockDetailUpdateThread(this.updateHandler, manager, ticker);
		thread.setName("StockDetailUpdate");
		thread.start();
	}
	
	// Define the Handler that receives messages from the thread
    final Handler updateHandler = new Handler() {
        public void handleMessage(Message msg) {
        	boolean result = msg.getData().getBoolean(StockListActivity.MSG_UPDATE_RESULT);
        	
        	if (result) {
            	final TextView txtPrice = (TextView) StockDetailActivity.this.findViewById(R.id.txtClosingPrice);
            	final TextView txtDate = (TextView) StockDetailActivity.this.findViewById(R.id.txtDetailDate);
            	
            	String price = msg.getData().getString("price");
            	String date = msg.getData().getString("date");
            	float change = msg.getData().getFloat("change");
            	Boolean positiveChange = change > 0;
            	
            	txtPrice.setText(price + "  (" + change + "%)");
            	txtDate.setText(date);
            	
            	if (positiveChange)
            		txtPrice.setTextColor(Color.GREEN);
            	else
            		txtPrice.setTextColor(Color.RED);
        	}

            else {
            	String failMessage = StockDetailActivity.this.getString(R.string.failed_price_update);
            	if (msg.getData().containsKey("message"))
            		failMessage += "\n" + msg.getData().getString("message");
        		Toast.makeText(StockDetailActivity.this, failMessage, Toast.LENGTH_LONG).show();
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
				DayData data = this.dataManager.getLastValue(this.ticker);
				float value = data.getPrice();
				DateFormat dateFormat = DateFormat.getDateInstance();
				String date = dateFormat.format(data.getDate());
				
				bundle.putString("price", String.valueOf(value));
				bundle.putBoolean("positiveChange", data.getChange() > 0);
				bundle.putFloat("change", data.getChange());
				bundle.putString("date", date);
				bundle.putBoolean("result", true);
			} catch (Exception e) {
				bundle.putBoolean("result", false);
				bundle.putString("message", e.getMessage());
			}
			
			msg.setData(bundle);
			if (this.handler != null) {
				handler.sendMessage(msg);
			}
		}
	}
}
