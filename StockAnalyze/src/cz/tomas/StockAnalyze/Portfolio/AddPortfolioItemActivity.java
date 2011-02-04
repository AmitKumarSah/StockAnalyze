/**
 * 
 */
package cz.tomas.StockAnalyze.Portfolio;

import java.sql.SQLException;
import java.util.Calendar;

import cz.tomas.StockAnalyze.PortfolioActivity;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.PortfolioItem;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.utils.Utils;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author tomas
 *
 */
public final class AddPortfolioItemActivity extends Activity {

	DataManager dataManager = null;
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.dataManager = DataManager.getInstance(this);
		this.setContentView(R.layout.portfolio_add_item_layout);
		
		Intent intent = this.getIntent();
		if (intent != null && intent.hasExtra("stock_id") && intent.hasExtra("market_id")) {
			final Market market = (Market) intent.getExtras().getSerializable("market_id");
			final String stockId = intent.getStringExtra("stock_id");
			final StockItem stockItem = this.dataManager.getStockItem(stockId, market);
			
			final TextView tickerView = (TextView) this.findViewById(R.id.portfolioAddTicker);
			final TextView marketView = (TextView) this.findViewById(R.id.portfolioAddMarket);
			final TextView priceView = (TextView) this.findViewById(R.id.portfolioAddPrice);
			final TextView countView = (TextView) this.findViewById(R.id.portfolioAddCount);
			final Button addButton = (Button) this.findViewById(R.id.portfolioAddButton);
			
			if (marketView != null && market != null)
				marketView.setText(market.getName());
			if (stockItem != null) {
				if (tickerView != null)
					tickerView.setText(stockItem.getTicker());
				if (priceView != null)
					try {
						priceView.setText(String.valueOf(this.dataManager.getLastValue(stockItem).getPrice()));
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
			if (addButton != null)
				addButton.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						addButton.setEnabled(false);
						// check if all fields are filled
						if (priceView.getText() == null || countView.getText() == null ||
								marketView.getText() == null || tickerView.getText() == null)
							Toast.makeText(AddPortfolioItemActivity.this, R.string.portfolioValidationMessage, Toast.LENGTH_SHORT).show();
						else if (market != null && stockId != null) {
							try {
								int count = Integer.parseInt(countView.getText().toString());
								float price = Float.parseFloat(priceView.getText().toString());
								
								addPortfolioItem(stockId, count, price, "default", market.getId());
								
								Intent intent = new Intent(AddPortfolioItemActivity.this, PortfolioActivity.class);
								AddPortfolioItemActivity.this.startActivity(intent);
							} catch (NumberFormatException e) {
								e.printStackTrace();
								Log.d(Utils.LOG_TAG, "failed to parse data from add portfolio layout");
							}
						}
						addButton.setEnabled(true);
						return;
					}
				});
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	private void addPortfolioItem(String stockId, int count, float price, String portfolioName, String marketId) {
		Portfolio portfolio = new Portfolio(this);
		
		// construct portfolio item and pass it to Portfolio
		PortfolioItem item = new PortfolioItem(stockId, portfolioName, count, price, 
				Calendar.getInstance().getTimeInMillis(), marketId);
		try {
			portfolio.addToPortfolio(item);
		} catch (SQLException e) {
			e.printStackTrace();
			String message = this.getText(R.string.portfolioFailedToAdd).toString();
			if (e.getMessage() != null)
				message += e.getMessage();
			
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}
	}
	
}
