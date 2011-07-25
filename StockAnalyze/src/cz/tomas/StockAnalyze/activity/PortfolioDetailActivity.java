package cz.tomas.StockAnalyze.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListView;
import android.widget.TextView;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.Portfolio.PortfolioDetailListAdapter;
import cz.tomas.StockAnalyze.charts.view.CompositeChartView;
import cz.tomas.StockAnalyze.utils.Utils;


public class PortfolioDetailActivity extends ChartActivity {

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.portfolio_detail_layout);
		
		
		try {
			StockItem stockItem = getIntent().getParcelableExtra(PortfolioActivity.EXTRA_STOCK_ITEM);
			this.stockItem = stockItem;
			TextView title = (TextView) this.findViewById(R.id.portfolioDetailTitle);
			title.setText(stockItem.getTicker());
			
			PortfolioDetailListAdapter adapter = new PortfolioDetailListAdapter(this, stockItem);
			
			ExpandableListView epView = (ExpandableListView) findViewById(R.id.portfolioDetailList);
			epView.setAdapter(adapter);
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to initialize portfolio detail");
		}

		this.chartView = (CompositeChartView) this.findViewById(R.id.portfolioDetailChartView);
		if (chartView != null && this.stockItem != null) {
			this.registerForContextMenu(this.chartView);
			this.updateChart();
		} else
			Log.w(Utils.LOG_TAG, "Failed to initialize chart view");
	}
}
