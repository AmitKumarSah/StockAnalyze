/*******************************************************************************
 * Copyright (c) 2011 Tomas Vondracek.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Tomas Vondracek
 ******************************************************************************/
/**
 * 
 */
package cz.tomas.StockAnalyze.activity;

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
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.UpdateScheduler;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Interfaces.IListAdapterListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Portfolio.AddPortfolioItemActivity;
import cz.tomas.StockAnalyze.R.id;
import cz.tomas.StockAnalyze.R.layout;
import cz.tomas.StockAnalyze.R.menu;
import cz.tomas.StockAnalyze.R.string;
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
						Log.d(Utils.LOG_TAG, "Failed to get TabActivity");
				}
				else if (stock != null) {
					NavUtils.goToStockDetail(stock, adapter.getDayData(stock), StockListActivity.this);
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

		if (! Utils.isOnline(this)) {
			//this.showDialog(NO_INTERNET);
			Toast.makeText(this, R.string.NoInternet, Toast.LENGTH_SHORT).show();
		}

		this.fill();
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
		DayData data = adapter.getDayData(stockItem);
		
		if (stockItem == null)
			return true;
		
		switch (item.getItemId()) {
			case R.id.stock_item_add_to_portfolio:
				Intent intent = new Intent();
				intent.putExtra(Utils.EXTRA_STOCK_ITEM, stockItem);
				intent.putExtra(Utils.EXTRA_DAY_DATA, data);
				intent.putExtra(Utils.EXTRA_MARKET_ID, stockItem.getMarket());
				intent.setClass(this, AddPortfolioItemActivity.class);
				startActivity(intent);
				return true;
			case R.id.stock_item_favourite:
				// TODO mark as favourite
				return true;
			case R.id.stock_item_view:
				NavUtils.goToStockDetail(stockItem, StockListActivity.adapter.getDayData(stockItem), this);
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
	    	UpdateScheduler scheduler = 
	    		(UpdateScheduler) this.getApplicationContext().getSystemService(Application.UPDATE_SCHEDULER_SERVICE);
	    	scheduler.updateImmediatly();
	        return true;
	    case R.id.menu_stock_list_settings:
	    	NavUtils.goToSettings(this);
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

