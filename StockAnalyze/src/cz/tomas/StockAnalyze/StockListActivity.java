/**
 * 
 */
package cz.tomas.StockAnalyze;

import cz.tomas.StockAnalyze.Data.DataManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebIconDatabase.IconListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

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
		
		String[] test = new String[] { "BAACEZ", "BAATELEC", "BAACETV" };
		this.setListAdapter(new ArrayAdapter<String>(this, R.layout.stock_list, test));
		
		this.getListView().setTextFilterEnabled(true);
		
		this.getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				if (StockListActivity.this.getParent() instanceof TabActivity) {
					// set currently selected ticker
					Object obj = StockListActivity.this.getListView().getItemAtPosition(position);
					
					TabActivity act = (TabActivity) StockListActivity.this.getParent();
					act.getIntent().putExtra("ticker", obj.toString());
					act.getTabHost().setCurrentTabByTag("StockDetail");
				}
			}
		});

		StockListUpdateThread thread = new StockListUpdateThread(this.updateHandler, this.dataManager);
		thread.setName("StockUpdate");
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
			builder.setMessage("The data has been successfuly updated!");
			dlg = builder.create();
			break;
		case UPDATE_DLG_FAIL:
			builder.setMessage("An error occured while updating the data!");
			dlg = builder.create();
			break;
		}
		
		return dlg;
	}
	
    // Define the Handler that receives messages from the thread
    final Handler updateHandler = new Handler() {
        public void handleMessage(Message msg) {
        	boolean result = msg.getData().getBoolean(StockListActivity.MSG_UPDATE_RESULT);
            
        	if (result) {
            	//StockListActivity.this.showDialog(StockListActivity.UPDATE_DLG_SUCCES);
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
		
		public StockListUpdateThread(Handler handler, DataManager dataManager) {
			this.handler = handler;
			this.dataManager = dataManager;
		}
		
		@Override
		public void run()
		{			
			Message msg = new Message();
			Bundle bundle = new Bundle();
			
			try {
				dataManager.getLastValue("BAACEZ");
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

