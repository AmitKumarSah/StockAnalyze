/**
 * 
 */
package cz.tomas.StockAnalyze.activity;

import java.text.NumberFormat;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
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
import cz.tomas.StockAnalyze.R.id;
import cz.tomas.StockAnalyze.R.layout;
import cz.tomas.StockAnalyze.R.menu;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.NavUtils;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * Activity showing all user's portfolios
 * 
 * @author tomas
 *
 */
public class PortfolioActivity extends ListActivity {

	private DataManager dataManager;
	private static Portfolio portfolio;
	//private static PortfolioSum portfolioSummary;
	private static PortfolioListAdapter adapter;
	private static View headerView;
	private static View footerView;
	private static boolean isDirty;
	
	private ProgressBar progressBar;
	
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

		if (headerView == null)
			headerView = vi.inflate(R.layout.portfolio_list_header, null);
		if (footerView == null)
			footerView = vi.inflate(R.layout.portfolio_list_footer, null);
		this.getListView().addHeaderView(headerView, null, false);
		this.getListView().addFooterView(footerView, null, false);

		if (portfolio == null)
			portfolio = new Portfolio(this);
		this.fill();
	}

	/* 
	 * check if it is necessary to updated the adapter and listview
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		this.fill();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private void fill() {
		if (adapter == null) {
			// this should be done only one-time
			if (progressBar != null)
				progressBar.setVisibility(View.VISIBLE);
			this.dataManager.addUpdateChangedListener(new IUpdateDateChangedListener() {
				
				@Override
				public void OnLastUpdateDateChanged(long updateTime) {
					Log.d(Utils.LOG_TAG, "refreshing portfolio list adapter because of datamanager update");
					//fill(true);
					isDirty = true;
				}
			});
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

		if (isDirty) {
			adapter.refresh();
			isDirty = false;
		}
//		if (portfolioSummary != null)
//			this.fillPortfolioSummary(portfolioSummary);
		// in case of resuming when adapter is initialized but not set to list view
		if (this.getListAdapter() == null) {
			this.setListAdapter(adapter);
		}
	}
	
	/* stock
	 * context menu for stock item
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		PortfolioItem portfolioItem = (PortfolioItem) this.getListAdapter().getItem(info.position - 1);
		
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
				// TODO own thread
				this.portfolio.removeFromPortfolio(portfolioItem.getId());
				this.adapter.refresh();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				return true;
			case R.id.portfolio_item_context_menu_detail:
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
		inflater.inflate(R.menu.portfolio_item_context_menu, menu);
	}

	/*
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
	    	PortfolioActivity.adapter.refresh();
	        return true;
	    case R.id.menu_portfolio_settings:
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	private void fillPortfolioSummary(PortfolioSum portfolioSummary) {
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
}
