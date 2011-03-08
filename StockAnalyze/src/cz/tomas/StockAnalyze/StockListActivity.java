/**
 * 
 */
package cz.tomas.StockAnalyze;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ProgressBar;
import android.widget.Toast;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.UpdateScheduler;
import cz.tomas.StockAnalyze.Data.Interfaces.IListAdapterListener;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Portfolio.AddPortfolioItemActivity;
import cz.tomas.StockAnalyze.StockList.StockListAdapter;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * @author tomas
 *
 */
public class StockListActivity extends ListActivity {

	static final int UPDATE_DLG_SUCCES = 0;
	static final int UPDATE_DLG_FAIL = 1;
	static final int NO_INTERNET = 2;
	
	DataManager dataManager;
	ProgressBar progressBar;
	
	static StockListAdapter adapter;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Debug.startMethodTracing();
		
		this.dataManager = DataManager.getInstance(this);
		
		this.setContentView(R.layout.stock_list);
		this.getListView().setTextFilterEnabled(true);
		this.registerForContextMenu(this.getListView());
		
		try {
			this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to find progress bar", e);
		}

		this.getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				StockItem stock = (StockItem) StockListActivity.this.getListView().getItemAtPosition(position);
				// old ui uses tabhost, new one doesn't
				if (stock != null && StockListActivity.this.getParent() instanceof TabActivity) {
					// set currently selected ticker
					
					TabActivity act = (TabActivity) StockListActivity.this.getParent();
					if (act != null) {
						act.getIntent().putExtra("stock_id", stock.getId());
						act.getIntent().putExtra("market_id", stock.getMarket());
						act.getTabHost().setCurrentTabByTag("StockDetail");
					}
					else
						Log.d("cz.tomas.StockAnalyze.StockListActivity", "Failed to get TabActivity");
				}
				else if (stock != null) {
					NavUtils.goToStockDetail(stock, StockListActivity.this);
				}
			}

		});
		
	}

	private void fill() {
		if (adapter == null) {
			if (progressBar != null)
				progressBar.setVisibility(View.VISIBLE);
			// this should be done only one-time
			adapter = new StockListAdapter(this, R.layout.stock_list, this.dataManager, "*");	//TODO replace string with filter
			adapter.addListAdapterListener( new IListAdapterListener<Object>() {
				
				@Override
				public void onListLoading() {
					if (progressBar != null)
						progressBar.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onListLoaded(Object data) {
					if (progressBar != null)
						progressBar.setVisibility(View.GONE);
				}
			});
			adapter.showIcons(false);
		}

		// in case of resuming when adapter is initialized but not set to list view
		if (this.getListAdapter() == null) 
			this.setListAdapter(adapter);
	}
	
	@Override
	public void onResume() {
		super.onResume();

		if (!this.dataManager.isOnline(this))
			this.showDialog(NO_INTERNET);

		this.fill();
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		//Debug.stopMethodTracing();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.stock_list_menu, menu);
	    return true;
	}
	
	
	/* stock
	 * context menu for stock item
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		StockItem stockItem = (StockItem) this.getListAdapter().getItem(info.position);
		
		if (stockItem == null)
			return true;
		
		switch (item.getItemId()) {
			case R.id.stock_item_add_to_portfolio:
				Intent intent = new Intent();
				intent.putExtra("stock_id", stockItem.getId());
				intent.putExtra("market_id", stockItem.getMarket());
				intent.setClass(this, AddPortfolioItemActivity.class);
				startActivity(intent);
				return true;
			case R.id.stock_item_favourite:
				// TODO mark as favourite
				return true;
			case R.id.stock_item_view:
				NavUtils.goToStockDetail(stockItem, this);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	/* 
	 * context menu for all stock items in list view
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.stock_item_context_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_stock_list_refresh:
//	    	RefreshTask task = new RefreshTask();
//	    	task.execute((Void[]) null);
	    	UpdateScheduler.getInstance(this).updateImmediatly();
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
	
	class RefreshTask extends AsyncTask<Void, Integer, Boolean> {

		/* 
		 * clear the list view
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

	    	//setListAdapter(null);
//			if (StockListActivity.adapter != null)
//				StockListActivity.adapter.clear();
		}

		/* 
		 * just show error message
		 * @see android.os.AsyncTask#onCancelled()
		 */
		@Override
		protected void onCancelled() {
			super.onCancelled();
			Toast.makeText(getParent(), R.string.update_fail, Toast.LENGTH_LONG).show();
		}

		/*
		 * invoke global data refresh
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Boolean doInBackground(Void... params) {
			boolean result = false;
			try {
				result = dataManager.refresh();
			} catch (Exception e) {
				e.printStackTrace();
				if (e.getMessage() != null)
					Log.d("StockListlActivity", e.getMessage());
			}
			return result;
		}

		/* 
		 * adapter should get updated on itself, just show proper message
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result){
		    	fill();
		    	Toast.makeText(StockListActivity.this, R.string.update_succes, Toast.LENGTH_SHORT).show();
			}
			else
				Toast.makeText(StockListActivity.this, R.string.NoRefresh, Toast.LENGTH_SHORT).show();
		}
		
	}
}

