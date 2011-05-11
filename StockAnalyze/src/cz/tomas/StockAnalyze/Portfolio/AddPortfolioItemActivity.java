/**
 * 
 */
package cz.tomas.StockAnalyze.Portfolio;

import java.sql.SQLException;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import cz.tomas.StockAnalyze.R;
import cz.tomas.StockAnalyze.Data.DataManager;
import cz.tomas.StockAnalyze.Data.Model.Market;
import cz.tomas.StockAnalyze.Data.Model.PortfolioItem;
import cz.tomas.StockAnalyze.Data.Model.StockItem;
import cz.tomas.StockAnalyze.activity.PortfolioActivity;
import cz.tomas.StockAnalyze.utils.FormattingUtils;
import cz.tomas.StockAnalyze.utils.Utils;

/**
 * @author tomas
 *
 */
public final class AddPortfolioItemActivity extends Activity {

	private DataManager dataManager = null;
	private TextView totalValueView = null;
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//this.dataManager = (DataManager) this.getSystemService(Application.DATA_MANAGER_SERVICE);
		this.dataManager = DataManager.getInstance(this);
		this.setContentView(R.layout.portfolio_add_item_layout);
		
		Intent intent = this.getIntent();
		if (intent != null && intent.hasExtra("stock_id") && intent.hasExtra("market_id")) {
			final Market market = (Market) intent.getExtras().getSerializable("market_id");
			final String stockId = intent.getStringExtra("stock_id");
			final StockItem stockItem = this.dataManager.getStockItem(stockId, market);
			
			this.totalValueView = (TextView) this.findViewById(R.id.portfolioAddTotalValue);
			final TextView tickerView = (TextView) this.findViewById(R.id.portfolioAddTicker);
			final TextView marketView = (TextView) this.findViewById(R.id.portfolioAddMarket);
			final TextView priceView = (TextView) this.findViewById(R.id.portfolioAddPrice);
			final TextView countView = (TextView) this.findViewById(R.id.portfolioAddCount);
			final TextView feeView = (TextView) this.findViewById(R.id.portfolioAddDealFee);
			final Button addButton = (Button) this.findViewById(R.id.portfolioAddButton);
			final Spinner dealSpinner = (Spinner) this.findViewById(R.id.portfolioAddSpinnerDeal);
			float stockPrice = 0;
			try {
				stockPrice = this.dataManager.getLastValue(stockItem).getPrice();
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "failed to get stock day data", e);
			}
			
			if (marketView != null && market != null)
				marketView.setText(market.getName());
			if (stockItem != null) {
				if (tickerView != null)
					tickerView.setText(stockItem.getTicker());
				if (priceView != null)
					priceView.setText(String.valueOf(stockPrice));
			}

			if (countView != null) {
				countView.setOnFocusChangeListener(new OnFocusChangeListener() {
					
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (! hasFocus)
							try {
								final float price = Float.parseFloat(priceView.getText().toString());
								final int count = Integer.parseInt(countView.getText().toString());
								final float fee = calculateFee(price, count);
								
								feeView.setText(String.valueOf(fee));

								updateTotalValue(price, count, fee);
							} catch (Exception e) {
								Log.e(Utils.LOG_TAG, "failed to set fee text", e);
							}
					}
				});
			}
			if (priceView != null) {
				priceView.setOnFocusChangeListener(new OnFocusChangeListener() {
					
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (! hasFocus) {
							try {
								final int count = Integer.parseInt(countView.getText().toString());
								final float price = Float.parseFloat(priceView.getText().toString());
								
								final float fee = calculateFee(price, count);
								feeView.setText(String.valueOf(fee));
								
								updateTotalValue(price, count, fee);
							} catch (Exception e) {
								Log.e(Utils.LOG_TAG, "failed to set total value", e);
							}
						}
					}
				});
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
								if (((String) dealSpinner.getSelectedItem()).equalsIgnoreCase("sell"))
									price = -price;
								float fee = calculateFee(price, count);
								addPortfolioItem(stockId, count, price, "default", market.getId(), fee);
								
								Intent intent = new Intent(AddPortfolioItemActivity.this, PortfolioActivity.class);
								intent.putExtra("refresh", true);
								AddPortfolioItemActivity.this.startActivity(intent);
							} catch (NumberFormatException e) {
								Log.e(Utils.LOG_TAG, "failed to parse data from add portfolio layout", e);
							}
						}
						addButton.setEnabled(true);
						return;
					}
				});
			try {
				final float price = Float.parseFloat(priceView.getText().toString());
				final int count = Integer.parseInt(countView.getText().toString());
				final float fee = calculateFee(price, count);
				
				feeView.setText(FormattingUtils.getPercentFormat().format(fee));
				updateTotalValue(price, count, fee);
			} catch (Exception e) {
				Log.e(Utils.LOG_TAG, "failed to fill values", e);
			}
		}
	}

	protected void updateTotalValue(final float price, final int count, final float fee) {
		float value = price * count + fee;
		
		try {
			this.totalValueView.setText(FormattingUtils.getPriceFormatCzk().format(value));
		} catch (Exception e) {
			Log.e(Utils.LOG_TAG, "failed to set total value", e);
		}
	}

	/* 
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

	private void addPortfolioItem(String stockId, int count, float price, String portfolioName, String marketId, float fee) {
		Portfolio portfolio = new Portfolio(this);
		
		// construct portfolio item and pass it to Portfolio
		PortfolioItem item = new PortfolioItem(stockId, portfolioName, count, price, 
				Calendar.getInstance().getTimeInMillis(), marketId);
		if (count > 0)
			item.setBuyFee(fee);
		else
			item.setSellFee(fee);

		try {
			dataManager.acquireDb(this.getClass().getName());
			portfolio.addToPortfolio(item);
		} catch (SQLException e) {
			Log.e(Utils.LOG_TAG, "failed to add portoflio item to db", e);
			String message = this.getText(R.string.portfolioFailedToAdd).toString();
			if (e.getMessage() != null)
				message += e.getMessage();
			
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		} finally {
			this.dataManager.releaseDb(true, this.getClass().getName());
		}
	}

	/**
	 * @param price
	 * @param count
	 * @return
	 */
	private float calculateFee(final float price, final int count) {
		float fee = (price * count) * 0.001f;
		return fee;
	}
	
}
