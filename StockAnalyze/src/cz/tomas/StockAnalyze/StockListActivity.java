/**
 * 
 */
package cz.tomas.StockAnalyze;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.StockList.StockListAdapter;

/**
 * @author tomas
 *
 */
public class StockListActivity extends ListActivity {

	static final int UPDATE_DLG_SUCCES = 0;
	static final int UPDATE_DLG_FAIL = 1;
	static final int NO_INTERNET = 2;
	
	DataManager dataManager;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.dataManager = new DataManager(this);
		
		fill();
		this.getListView().setTextFilterEnabled(true);
		this.setContentView(R.layout.stock_list);

		this.getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				if (StockListActivity.this.getParent() instanceof TabActivity) {
					// set currently selected ticker
					StockItem stock = (StockItem) StockListActivity.this.getListView().getItemAtPosition(position);
					
					TabActivity act = (TabActivity) StockListActivity.this.getParent();
					if (act != null) {
						act.getIntent().putExtra("stock_id", stock.getId());
						act.getTabHost().setCurrentTabByTag("StockDetail");
					}
					else
						Log.d("cz.tomas.StockAnalyze.StockListActivity", "Failed to get TabActivity");
				}
			}
		});
		
	}

	private synchronized void fill() {
		StockListAdapter adapter = new StockListAdapter(this, R.layout.stock_list, this.dataManager, "baa");	//TODO replace string with filter
		this.setListAdapter(adapter);
	}
	
	@Override
	public void onResume() {
		super.onResume();

		if (!this.dataManager.isOnline(this))
			this.showDialog(NO_INTERNET);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.stock_list_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_stock_list_refresh:
	    	try {
				if (this.dataManager.refresh()){
			    	this.setListAdapter(null);
			    	this.findViewById(R.id.progressStockList).setVisibility(View.VISIBLE);
			    	this.fill();
			    	Toast.makeText(this.getParent(), R.string.update_succes, Toast.LENGTH_LONG).show();
				}
				else
					Toast.makeText(this.getParent(), R.string.NoRefresh, Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Toast.makeText(this.getParent(), R.string.update_fail, Toast.LENGTH_LONG).show();
			}
	        return true;
	    case R.id.menu_stock_list_settings:
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
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
		case NO_INTERNET:
			builder.setMessage(R.string.NoInternet);
			dlg = builder.create();
			break;
		}
		
		return dlg;
	}
}

