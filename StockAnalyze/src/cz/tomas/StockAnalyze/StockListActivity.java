/**
 * 
 */
package cz.tomas.StockAnalyze;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;

/**
 * @author tomas
 *
 */
public class StockListActivity extends ListActivity {

	static final int UPDATE_DLG_SUCCES = 0;
	static final int UPDATE_DLG_FAIL = 1;
	static final String MSG_UPDATE_RESULT = "result";
	static final String MSG_UPDATE_DETAIL = "detail";
	
	DataManager dataManager;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.dataManager = new DataManager(this);
		
		StockListAdapter adapter = new StockListAdapter(this, R.id.toptext, this.dataManager, "baa");	//TODO replace string with filter
		this.setListAdapter(adapter);
		this.getListView().setTextFilterEnabled(true);

		this.getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				if (StockListActivity.this.getParent() instanceof TabActivity) {
					// set currently selected ticker
					StockItem stock = (StockItem) StockListActivity.this.getListView().getItemAtPosition(position);
					
					TabActivity act = (TabActivity) StockListActivity.this.getParent();
					act.getIntent().putExtra("ticker", stock.getTicker());
					act.getTabHost().setCurrentTabByTag("StockDetail");
				}
			}
		});

		StockItem[] items = new StockItem[this.getListAdapter().getCount()];
		
		for (int i = 0; i < items.length; i++) {
			items[i] = (StockItem) this.getListAdapter().getItem(i);
		}

		StockListUpdateThread thread = new StockListUpdateThread(this.updateHandler, this.dataManager, items);
		thread.setName("StockListUpdate");
		thread.start();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dlg = null;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setPositiveButton("OK", new Dialog.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dlg, int arg1) {
				dlg.dismiss();
			}
		});
		
		switch (id)
		{
		case UPDATE_DLG_SUCCES:
			builder.setMessage(R.string.update_succes);
			dlg = builder.create();
			break;
		case UPDATE_DLG_FAIL:
			builder.setMessage(R.string.update_fail);
			dlg = builder.create();
			break;
		}
		
		return dlg;
	}
	
    // Define the Handler that receives messages from the thread
    final Handler updateHandler = new Handler() {
        public void handleMessage(Message msg) {
        	boolean result = msg.getData().getBoolean(StockListActivity.MSG_UPDATE_RESULT);
        	ArrayAdapter adapter =  (ArrayAdapter) StockListActivity.this.getListAdapter();
        	
        	for (int i = 0; i < adapter.getCount(); i++) {
        		
			}
        	
        	if (result) {
        		Toast.makeText(StockListActivity.this, "The data was succesfully updated!", Toast.LENGTH_LONG).show();
        	}
            else {
            	StockListActivity.this.showDialog(StockListActivity.UPDATE_DLG_FAIL);
            }
            
        }
    };

	private class StockListUpdateThread extends Thread {
		Handler handler;
		DataManager dataManager;
		StockItem[] tickers;
		
		public StockListUpdateThread(Handler handler, DataManager dataManager, StockItem[] tickers) {
			this.handler = handler;
			this.dataManager = dataManager;
			this.tickers = tickers;
		}
		
		@Override
		public void run()
		{			
			Message msg = new Message();
			Bundle bundle = new Bundle();
			
			try {
				for (int i = 0; i < this.tickers.length; i++) {
					String ticker = this.tickers[i].getTicker();
					DayData data = dataManager.getLastValue(ticker);
					float value = data.getPrice();
					String date = data.getDate().toString();
					bundle.putFloat(ticker, value);
					bundle.putString(ticker + "date", date);
					
					Log.d("StockList", ticker + " updated to " + value);
				}
				bundle.putBoolean(MSG_UPDATE_RESULT, true);
			} catch (Exception e) {
				e.printStackTrace();
				bundle.putBoolean(MSG_UPDATE_RESULT, false);
				bundle.putString(MSG_UPDATE_DETAIL, e.getMessage());
			}
			
			msg.setData(bundle);
			if (this.handler != null) {
				handler.sendMessage(msg);
			}
		}
	}
}

