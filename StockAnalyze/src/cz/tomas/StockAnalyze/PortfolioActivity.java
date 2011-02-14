/**
 * 
 */
package cz.tomas.StockAnalyze;

import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.PortfolioItem;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Portfolio.AddPortfolioItemActivity;
import cz.tomas.StockAnalyze.Portfolio.Portfolio;
import cz.tomas.StockAnalyze.Portfolio.PortfolioListAdapter;
import cz.tomas.StockAnalyze.StockList.StockListAdapter;
import cz.tomas.StockAnalyze.utils.NavUtils;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * @author tomas
 *
 */
public class PortfolioActivity extends ListActivity {

	DataManager dataManager;
	Portfolio portfolio;
	
	static PortfolioListAdapter adapter;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.stock_list);
		this.getListView().setTextFilterEnabled(true);
		this.registerForContextMenu(this.getListView());
		
		this.dataManager = DataManager.getInstance(this);
		boolean refresh = this.getIntent().getBooleanExtra("refresh", false);
		
		LayoutInflater vi = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View headerView = vi.inflate(R.layout.portfolio_list_header, null);
		View footerView = vi.inflate(R.layout.portfolio_list_footer, null);
		this.getListView().addHeaderView(headerView, null, false);
		this.getListView().addFooterView(footerView, null, false);
		

		this.portfolio = new Portfolio(this);
		this.fill(refresh);
	}

	/* 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		
		this.fill(false);
	}



	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private void fill(boolean refresh) {
		if (adapter == null) {
			// this should be done only one-time
			//this.findViewById(R.id.progressStockList).setVisibility(View.VISIBLE);
			adapter = new PortfolioListAdapter(this, R.layout.stock_list, this.dataManager, this.portfolio);
		}

		if (refresh)
			adapter.refresh();
		
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
}
