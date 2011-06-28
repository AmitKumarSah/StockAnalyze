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

import java.text.NumberFormat;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Interfaces.IListAdapterListener;
import cz.tomas.StockAnalyze.Data.Interfaces.IUpdateDateChangedListener;
import cz.tomas.StockAnalyze.Data.Model.PortfolioItem;
import cz.tomas.StockAnalyze.Data.Model.PortfolioSum;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Portfolio.Portfolio;
import cz.tomas.StockAnalyze.Portfolio.PortfolioListAdapter;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * Activity showing all user's portfolios
 * 
 * @author tomas
 *
 */
public class PortfolioActivity extends ListActivity implements OnSharedPreferenceChangeListener {

	//private static final String INSTANCE_PORTFOLIO_SUMMARY = "portfolioSummary";
	
	private DataManager dataManager;
	private Portfolio portfolio;
	private PortfolioSum portfolioSummary;
	private PortfolioListAdapter adapter;
	private View headerView;
	private View footerView;
	private static boolean isDirty;
	
	private ProgressBar progressBar;
	private SharedPreferences prefs;
	
	/* 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.portfolio_list);
		this.getListView().setTextFilterEnabled(true);
		this.registerForContextMenu(this.getListView());
		
		this.dataManager = DataManager.getInstance(this);
		
		isDirty |= this.getIntent().getBooleanExtra("refresh", false);
		
		LayoutInflater vi = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.progressBar = (ProgressBar) findViewById(R.id.progressBar);

		headerView = vi.inflate(R.layout.portfolio_list_header, null);
		footerView = vi.inflate(R.layout.portfolio_list_footer, null);
		
		this.getListView().addHeaderView(headerView, null, false);
		this.getListView().addFooterView(footerView, null, false);
		this.getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position,
					long id) {
				final PortfolioItem portfolioItem = (PortfolioItem) getListAdapter().getItem(position -1);
				goToPortfolioDetail(portfolioItem);
			}
		});

		if (portfolio == null)
			portfolio = new Portfolio(this);
		
		this.prefs = this.getSharedPreferences(Utils.PREF_NAME, 0);
		this.prefs.registerOnSharedPreferenceChangeListener(this);
		//this.fill();
	}

//	/* (non-Javadoc)
//	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
//	 */
//	@Override
//	protected void onSaveInstanceState(Bundle outState) {
//		if (this.portfolioSummary != null)
//			outState.putSerializable(INSTANCE_PORTFOLIO_SUMMARY, this.portfolioSummary);
//		super.onSaveInstanceState(outState);
//	}



	/** 
	 * check if it is necessary to update the adapter and listview
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		this.dataManager.addUpdateChangedListener(listener);
		this.fill();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		this.dataManager.removeUpdateChangedListener(listener);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		prefs.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	private void fill() {
		if (adapter == null) {
			adapter = new PortfolioListAdapter(this, R.layout.stock_list, this.dataManager, this.portfolio);
			adapter.addPortfolioListener(new IListAdapterListener<PortfolioSum>() {

				@Override
				public void onListLoading() {					
					if (progressBar != null)
						progressBar.setVisibility(View.VISIBLE);
				}

				@Override
				public void onListLoaded(PortfolioSum portfolioSummary) {
					Log.d(Utils.LOG_TAG, "Updating portfolio summary");
					fillPortfolioSummary(portfolioSummary);
					
					if (progressBar != null)
						progressBar.setVisibility(View.GONE);					
				}
			});
		}

		// restore portfolio summary, it should available in case of resuming
		if (adapter.getPortfolioSummary() != null) {
			this.fillPortfolioSummary(adapter.getPortfolioSummary());
		}
		
		if (isDirty) {
			adapter.refresh();
			isDirty = false;
		}
		// in case of resuming when adapter is initialized but not set to list view
		if (this.getListAdapter() == null) {
			this.setListAdapter(adapter);
		}
	}
	
	private IUpdateDateChangedListener listener = new IUpdateDateChangedListener() {
		
		@Override
		public void OnLastUpdateDateChanged(long updateTime) {
			Log.d(Utils.LOG_TAG, "refreshing portfolio list adapter because of datamanager update");
			isDirty = true;
		}
	};
	
	/* stock
	 * context menu for stock item
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		final PortfolioItem portfolioItem = (PortfolioItem) this.getListAdapter().getItem(info.position - 1);
		
		if (portfolioItem == null)
			return true;
		
		switch (item.getItemId()) {
			case R.id.portfolio_item_context_menu_stock_detail:
			if (portfolioItem.getStockId() != null) {
				StockItem stock = this.dataManager.getStockItem(portfolioItem.getStockId());
				NavUtils.goToStockDetail(stock, this);
			}
			return true;
			case R.id.portfolio_item_context_menu_remove:
			try {
				if (progressBar != null)
					progressBar.setVisibility(View.VISIBLE);
				AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {

						try {
							portfolio.removeFromPortfolio(portfolioItem.getId());
						} catch (Exception e) {
							Log.e(Utils.LOG_TAG, "failed to remove portfolio item", e);
						}
						return null;
					}
					@Override
					protected void onPostExecute(Void result) {
						adapter.refresh();
						if (progressBar != null)
							progressBar.setVisibility(View.GONE);
						super.onPostExecute(result);
					}
					
				};
				task.execute((Void[])null);
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "failed to remove portfolio item", e);
			}
				return true;
			case R.id.portfolio_item_context_menu_detail:
			goToPortfolioDetail(portfolioItem);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	/**
	 * 
	 */
	private void goToPortfolioDetail(PortfolioItem item) {
		Intent intent = new Intent(this, PortfolioDetailActivity.class);
		this.startActivity(intent);
	}

	/** 
	 * context menu for all stock items in list view
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.portfolio_item_context_menu, menu);
	}

	/**
	 * create activity's main menu
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.portfolio_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_portfolio_refresh:
	    	this.adapter.refresh();
	        return true;
	    case R.id.menu_portfolio_settings:
	    	NavUtils.goToSettings(this);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	/**
	 * fill text views with portfolio summary
	 * @param portfolioSummary
	 */
	private void fillPortfolioSummary(PortfolioSum portfolioSummary) {
		this.portfolioSummary = portfolioSummary;
		TextView txtValueSum = (TextView)findViewById(R.id.txtPortfolioFooterSumValue);
		TextView txtChangeSum = (TextView)findViewById(R.id.txtPortfolioFooterSumChange);
		
		NumberFormat percentFormat = FormattingUtils.getPercentFormat();
    	String strAbsChange = percentFormat.format(portfolioSummary.getTotalAbsChange());
    	String strChange = percentFormat.format(portfolioSummary.getTotalPercChange());
    	
    	if (txtValueSum != null)
    		txtValueSum.setText(String.valueOf(portfolioSummary.getTotalValue()));
    	if (txtChangeSum != null)
    		txtChangeSum.setText(String.format("%s (%s%%)", strAbsChange, strChange));
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals(Utils.PREF_PORTFOLIO_INCLUDE_FEE)) {
			PortfolioActivity.isDirty = true;
			this.fill();
		}
		
	}
}
