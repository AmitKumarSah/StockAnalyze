/*******************************************************************************
 * StockAnalyze for Android
 *     Copyright (C)  2011 Tomas Vondracek.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/**
 * 
 */
package cz.tomas.StockAnalyze.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import cz.tomas.StockAnalyze.Application;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.UpdateScheduler;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Interfaces.IListAdapterListener;
import cz.tomas.StockAnalyze.Data.Model.DayData;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.StockList.StockListAdapter;
import cz.tomas.StockAnalyze.activity.base.BaseListActivity;
import cz.tomas.StockAnalyze.ui.widgets.ActionBar;
import cz.tomas.StockAnalyze.ui.widgets.ActionBar.IActionBarListener;
import cz.tomas.StockAnalyze.utils.Markets;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * @author tomas
 *
 */
public class StockListActivity extends BaseListActivity implements IActionBarListener {

	static final int UPDATE_DLG_SUCCES = 0;
	static final int UPDATE_DLG_FAIL = 1;
	static final int NO_INTERNET = 2;
	
	protected DataManager dataManager;
	private View refreshButton;
	private Animation refreshAnim; 
	
	private StockListAdapter adapter;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.dataManager = (DataManager) getApplicationContext().getSystemService(Application.DATA_MANAGER_SERVICE);
		
		this.setContentView(R.layout.stock_list);
		this.getListView().setTextFilterEnabled(true);
		this.registerForContextMenu(this.getListView());
		
		try {
			this.refreshButton = findViewById(R.id.actionRefreshButton);
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to find refresh button", e);
		}
		ActionBar bar = (ActionBar) findViewById(R.id.stockListActionBar);
		if (bar != null)
			bar.setActionBarListener(this);

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

		this.fill();

		if (! Utils.isOnline(this)) {
			//this.showDialog(NO_INTERNET);
			Toast.makeText(this, R.string.NoInternet, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * fill list view
	 */
	protected void fill() {
		this.adapter = createListAdapter();
		this.adapter.addListAdapterListener( new IListAdapterListener<Object>() {
			
			@Override
			public void onListLoading() {
				if (refreshButton != null){
					refreshAnim = AnimationUtils.loadAnimation(StockListActivity.this, R.anim.refresh_rotate);
					refreshButton.startAnimation(refreshAnim);
				}
			}
			
			@Override
			public void onListLoaded(Object data) {
				if (refreshAnim != null)
					refreshAnim.setDuration(0);
			}
		});

		// in case of resuming when adapter is initialized but not set to list view
		if (this.getListAdapter() == null) 
			this.setListAdapter(adapter);
	}

	/**
	 * create adapter instance
	 */
	protected StockListAdapter createListAdapter() {
		StockListAdapter adapter = new StockListAdapter(this, R.layout.stock_list, this.dataManager, Markets.CZ, false);
		adapter.showIcons(false);
		return adapter;
	}

	@Override
	public void onResume() {
		super.onResume();
		adapter.attachToData();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		this.adapter.detachFromData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.stock_list_menu, menu);
	    return super.onCreateOptionsMenu(menu);
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
				NavUtils.goToAddToPortfolio(this, stockItem, data);
				return true;
			case R.id.stock_item_favourite:
				// TODO mark as favourite
				return true;
			case R.id.stock_item_view:
				NavUtils.goToStockDetail(stockItem, adapter.getDayData(stockItem), this);
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
	    	updateImmediatly();
	        return true;
	    case R.id.menu_stock_list_settings:
	    	NavUtils.goToSettings(this);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	/**
	 * call UpdateScheduller for immediate update
	 */
	protected void updateImmediatly() {
		UpdateScheduler scheduler = 
			(UpdateScheduler) this.getApplicationContext().getSystemService(Application.UPDATE_SCHEDULER_SERVICE);
		scheduler.updateImmediatly(Markets.CZ);
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

	@Override
	public void onAction(int viewId) {
		if (viewId == R.id.actionRefreshButton) {
			this.updateImmediatly();
		}
	}
	
}

