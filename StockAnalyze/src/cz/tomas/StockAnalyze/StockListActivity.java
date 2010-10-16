/**
 * 
 */
package cz.tomas.StockAnalyze;

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
					act.getIntent().putExtra("stock_id", stock.getId());
					act.getTabHost().setCurrentTabByTag("StockDetail");
				}
			}
		});
		
//		StockItem[] items = new StockItem[this.getListAdapter().getCount()];
//		
//		for (int i = 0; i < items.length; i++) {
//			items[i] = (StockItem) this.getListAdapter().getItem(i);
//		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
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
}

