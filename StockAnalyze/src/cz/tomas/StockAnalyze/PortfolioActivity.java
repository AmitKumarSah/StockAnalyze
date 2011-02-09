/**
 * 
 */
package cz.tomas.StockAnalyze;

import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Portfolio.PortfolioListAdapter;
import cz.tomas.StockAnalyze.StockList.StockListAdapter;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

/**
 * @author tomas
 *
 */
public class PortfolioActivity extends ListActivity {

	DataManager dataManager;
	
	static PortfolioListAdapter adapter;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.stock_list);
		this.getListView().setTextFilterEnabled(true);
		
		this.dataManager = DataManager.getInstance(this);
		boolean refresh = this.getIntent().getBooleanExtra("refresh", false);
		
		LayoutInflater vi = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View headerView = vi.inflate(R.layout.portfolio_list_header, null);
		View footerView = vi.inflate(R.layout.portfolio_list_footer, null);
		this.getListView().addHeaderView(headerView, null, false);
		this.getListView().addFooterView(footerView, null, false);
		
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

	private synchronized void fill(boolean refresh) {
		if (adapter == null) {
			// this should be done only one-time
			//this.findViewById(R.id.progressStockList).setVisibility(View.VISIBLE);
			adapter = new PortfolioListAdapter(this, R.layout.stock_list, this.dataManager);
		}

		if (refresh)
			adapter.refresh();
		
		// in case of resuming when adapter is initialized but not set to list view
		if (this.getListAdapter() == null) {
			this.setListAdapter(adapter);
		}
	}
}
